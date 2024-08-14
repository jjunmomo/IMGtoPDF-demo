package com.example.demo.pdf.util;

import com.example.demo.pdf.dto.PdfGenerationInfo;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PdfMergeUtil {

    public static void mergePdfDocuments(PDFMergerUtility mergerUtility, List<PdfGenerationInfo> pdfPages, OutputStream outputStream, String filePath) throws IOException {
        for (PdfGenerationInfo pdfInfo : pdfPages) {
            File file = new File(filePath + pdfInfo.getFileName());
            if (file.exists()) {
                mergerUtility.addSource(file);
            } else {
                System.out.println("File not found: " + file.getAbsolutePath());
            }
        }

        // 병합된 PDF를 메모리 스트림으로 저장
        mergerUtility.setDestinationStream(outputStream);
        mergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }

    public static void deleteIndividualPdfs(List<PdfGenerationInfo> pdfPages, String filePath) {
        for (PdfGenerationInfo pdfInfo : pdfPages) {
            File file = new File(filePath + pdfInfo.getFileName());
            if (file.exists()) {
                file.delete();
            }
        }
        pdfPages.clear(); // 병합 후 리스트를 초기화하여 기존 PDF 파일 목록을 비웁니다.
    }
}
