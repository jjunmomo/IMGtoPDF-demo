package com.example.demo.pdf.dto;

public class PdfGenerationInfo {

    private String fileName;
    private long generationTime; // 초 단위

    public PdfGenerationInfo(String fileName, long generationTime) {
        this.fileName = fileName;
        this.generationTime = generationTime;
    }

    public PdfGenerationInfo(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public long getGenerationTime() {
        return generationTime;
    }
}
