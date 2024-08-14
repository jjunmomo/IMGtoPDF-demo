//package com.example.demo.pdf.service;
//
//@Service
//public class PdfService {
//
//    public byte[] generatePdfFromUrl(String url) throws Exception {
//        Pdf pdf = new Pdf();
//
//        // URL을 통해 HTML 페이지를 가져와 PDF 변환
//        pdf.addPage(url);
//
//        // PDF 옵션 설정
//        pdf.addParam(new Param("--page-size", "A4"));
//
//        // PDF 생성 및 바이트 배열로 변환
//        return pdf.getPDF();
//    }
//}
