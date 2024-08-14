package com.example.demo.pdf.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PdfResponseUtil {

    public static ResponseEntity<byte[]> createResponse(byte[] pdfContent,
                                                        String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String encodedFileName = URLEncoder
                .encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFileName);

        // AOP에서 생성 시간을 헤더에 추가할 것입니다.
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
