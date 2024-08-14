package com.example.demo.pdf.controller;

import com.example.demo.pdf.aop.PDFTimeCheck;
import com.example.demo.pdf.dto.PdfGenerationInfo;
import com.example.demo.pdf.dto.ProposalComments;
import com.example.demo.pdf.util.PdfConstants;
import com.example.demo.pdf.util.PdfGeneratorUtil;
import com.example.demo.pdf.util.PdfMergeUtil;
import com.example.demo.pdf.util.PdfResponseUtil;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.pdfbox.io.MemoryUsageSetting;
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

    @PDFTimeCheck
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("image") MultipartFile imageFile,
                                              @RequestParam("content") String content,
                                              @RequestParam("modificationRequirements") String modificationRequirements) {
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
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

            PDType0Font font=PDType0Font.load(document, getClass().getResourceAsStream("/fonts/Pretendard-Regular.ttf"));
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getHeight();
            float pageHeight = page.getMediaBox().getWidth();

            PdfGeneratorUtil.resetCoordinateSystem(contentStream, pageHeight);

            int pageNumber = pdfGenerationInfoList.size() + 1;
            PdfGeneratorUtil.addHeader(contentStream, font, pageHeight, pageWidth, pageNumber);
            PdfGeneratorUtil.addFooter(contentStream, font, pageWidth);
            PdfGeneratorUtil.addSectionDividers(contentStream, pageWidth, pageHeight);

            float sectionHeight = pageHeight - PdfConstants.HEADER_HEIGHT - PdfConstants.FOOTER_HEIGHT;
            float sectionWidth = pageWidth / 3;

            PdfGeneratorUtil.addImage(contentStream, imageFile, document, sectionWidth, sectionHeight);

            ProposalComments comments = new ProposalComments(content, modificationRequirements);
            PdfGeneratorUtil.addTextToSections(contentStream, font, sectionWidth, pageHeight, comments);

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

//    @PDFTimeCheck
//    @PostMapping("/generate")
//    public ResponseEntity<byte[]> generatePdf(@RequestParam("image") MultipartFile imageFile,
//                                              @RequestParam("content") String content,
//                                              @RequestParam("modificationRequirements") String modificationRequirements) {
//        try (PDDocument document = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            document.addPage(page);
//            page.setRotation(90);
//
//            PDType0Font font = PDType0Font.load(document, new File("/System/Library/Fonts/Supplemental/Arial Unicode.ttf"));
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//            float pageWidth = page.getMediaBox().getHeight();
//            float pageHeight = page.getMediaBox().getWidth();
//
//            PdfGeneratorUtil.resetCoordinateSystem(contentStream, pageHeight);
//
//            int pageNumber = pdfGenerationInfoList.size() + 1;
//            PdfGeneratorUtil.addHeader(contentStream, font, pageHeight, pageWidth, pageNumber);
//            PdfGeneratorUtil.addFooter(contentStream, font, pageWidth);
//            PdfGeneratorUtil.addSectionDividers(contentStream, pageWidth, pageHeight);
//
//            float sectionHeight = pageHeight - 90;
//            float sectionWidth = pageWidth / 3;
//            PdfGeneratorUtil.addImage(contentStream, imageFile, document, sectionWidth, sectionHeight);
//
//            ProposalComments comments = new ProposalComments(content, modificationRequirements);
//            PdfGeneratorUtil.addTextToSections(contentStream, font, sectionWidth, pageHeight, comments);
//
//            contentStream.restoreGraphicsState();
//            contentStream.close();
//
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            document.save(byteArrayOutputStream);
//            byte[] pdfContent = byteArrayOutputStream.toByteArray();
//
//            String fileName = "generated_pdf_" + System.currentTimeMillis() + ".pdf";
//            File file = new File(path + fileName);
//            document.save(file);
//
//            PdfGenerationInfo pdfInfo = new PdfGenerationInfo(fileName);
//            pdfGenerationInfoList.add(pdfInfo);
//
//            return PdfResponseUtil.createResponse(pdfContent, fileName);
//        } catch (IOException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


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

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs() {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String mergedFileName = "merged_pdf_" + timeStamp + ".pdf";
        String mergedFilePath = path + mergedFileName;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TeeOutputStream teeOutputStream = new TeeOutputStream(byteArrayOutputStream, new FileOutputStream(mergedFilePath))) {

            PDFMergerUtility mergerUtility = new PDFMergerUtility();

            // PDF 파일 병합 수행 및 메모리와 파일로 동시에 저장
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
