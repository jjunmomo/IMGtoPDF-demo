package com.example.demo.pdf.controller;

import com.example.demo.pdf.aop.PDFTimeCheck;
import com.example.demo.pdf.dto.PdfGenerationInfo;
import com.example.demo.pdf.dto.ProposalComments;
import com.example.demo.pdf.util.PdfConstants;
import com.example.demo.pdf.util.PdfGeneratorUtil;
import com.example.demo.pdf.util.PdfMergeUtil;
import com.example.demo.pdf.util.PdfResponseUtil;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PdfController {

    private List<PdfGenerationInfo> pdfGenerationInfoList = new ArrayList<>();

    private String path;

    public PdfController() {
        this.path = System.getProperty("user.home") + File.separator + "pdf" + File.separator;
        createDirectoryIfNotExists(this.path);
    }

    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    //PDF 생성
    @PDFTimeCheck
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("image") MultipartFile imageFile,
                                              @RequestParam("content") String content,
                                              @RequestParam("modificationRequirements") String modificationRequirements) {
        try (PDDocument document = new PDDocument()) {

// 페이지 크기 커스텀 (1920x911)
            PDRectangle customSize = new PDRectangle(911, 1920);
            PDPage page = new PDPage(customSize);
            document.addPage(page);
            page.setRotation(90);
//
//            if (os.contains("win")) {
//                // 윈도우 시스템
//                System.out.println("win");
//                fontPath = "C:\\Windows\\Fonts\\malgun.ttf"; // 맑은 고딕 폰트
//            } else if (os.contains("mac")) {
//                // 맥OS 시스템
//                System.out.println("macOS");
//                fontPath ="/System/Library/Fonts/Supplemental/Arial Unicode.ttf";
//            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
//                // 리눅스 시스템
//                System.out.println("나머지");
//                fontPath = "/usr/share/fonts/truetype/nanum/NanumGothic.ttf"; // 나눔고딕 폰트 (리눅스)
//            }
//            PDType0Font font = PDType0Font.load(document, new File(fontPath));


            PDType0Font boldFont = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/Pretendard-Bold.ttf"));
            PDType0Font bodyFont = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/Pretendard-Regular.ttf"));
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getHeight();
            float pageHeight = page.getMediaBox().getWidth();

            PdfGeneratorUtil.resetCoordinateSystem(contentStream, pageHeight);

            int pageNumber = pdfGenerationInfoList.size() + 1;
            PdfGeneratorUtil.addHeader(contentStream, boldFont, pageHeight, pageWidth, pageNumber);
            PdfGeneratorUtil.addFooter(contentStream, boldFont , pageWidth);
            PdfGeneratorUtil.addSectionDividers(contentStream, pageWidth, pageHeight);

            float sectionHeight = pageHeight - PdfConstants.HEADER_HEIGHT - PdfConstants.FOOTER_HEIGHT;
            float sectionWidth = pageWidth / 3;

            PdfGeneratorUtil.addImage(contentStream, imageFile, document, sectionWidth, sectionHeight);

            ProposalComments comments = new ProposalComments(content, modificationRequirements);
            PdfGeneratorUtil.addTextToSections(contentStream, boldFont ,bodyFont, sectionWidth, pageHeight, comments);

            contentStream.restoreGraphicsState();
            contentStream.close();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            byte[] pdfContent = byteArrayOutputStream.toByteArray();

            String fileName = "generated_pdf_" + System.currentTimeMillis() + ".pdf";
            File file = new File(path + fileName);
            document.save(file);

            PdfGenerationInfo pdfInfo = new PdfGenerationInfo(fileName);
            pdfGenerationInfoList.add(pdfInfo);

            return PdfResponseUtil.createResponse(pdfContent, fileName);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //PDF 리스트 불러오기
    @GetMapping("/generated")
    public ResponseEntity<List<String>> getGeneratedPdfs() {
        List<String> encodedFileNames = pdfGenerationInfoList.stream()
                .map(info -> {
                    String encodedFileName = URLEncoder.encode(info.getFileName(), StandardCharsets.UTF_8);
                    return encodedFileName + "," + info.getGenerationTime() + "s";
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(encodedFileNames, HttpStatus.OK);
    }

    //PDF 리스트 병합
    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String mergedFileName = "merged_pdf_" + timeStamp + ".pdf";
        String mergedFilePath = path + mergedFileName;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(mergedFilePath);
             TeeOutputStream teeOutputStream = new TeeOutputStream(byteArrayOutputStream, fileOutputStream)) {

            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            mergerUtility.setDestinationStream(teeOutputStream);

            // 첫 페이지 추가
            try (InputStream firstPageStream = getClass().getResourceAsStream("/pdfs/first_page.pdf")) {
                if (firstPageStream != null) {
                    // 임시 파일에 리소스 PDF를 저장하고 병합에 추가
                    File tempFirstPage = File.createTempFile("first_page", ".pdf");
                    try (FileOutputStream tempOutStream = new FileOutputStream(tempFirstPage)) {
                        firstPageStream.transferTo(tempOutStream);
                    }
                    mergerUtility.addSource(tempFirstPage);
                } else {
                    throw new IOException("첫 페이지 PDF 파일을 찾을 수 없습니다.");
                }
            }

            // 기존 PDF 파일 병합 수행
            PdfMergeUtil.mergePdfDocuments(mergerUtility, pdfGenerationInfoList, teeOutputStream, path);

            // 개별 PDF 파일 삭제
            PdfMergeUtil.deleteIndividualPdfs(pdfGenerationInfoList, path);

            // 응답 생성
            return PdfResponseUtil.createResponse(byteArrayOutputStream.toByteArray(), mergedFileName);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
