package com.example.demo.pdf.util;

import com.example.demo.pdf.dto.ProposalComments;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PdfGeneratorUtil {

    public static void drawText(PDPageContentStream contentStream,
                                PDType0Font font,
                                String text,
                                float x,
                                float y,
                                int fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    public static void drawLine(PDPageContentStream contentStream,
                                float startX,
                                float startY,
                                float endX,
                                float endY) throws IOException {
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(PdfConstants.LINE_WIDTH);
        contentStream.moveTo(startX, startY);
        contentStream.lineTo(endX, endY);
        contentStream.stroke();
    }

    public static void resetCoordinateSystem(PDPageContentStream contentStream,
                                             float pageHeight) throws IOException {
        contentStream.saveGraphicsState();
        contentStream.transform(Matrix.getTranslateInstance(pageHeight, 0));
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(90), 0, 0));
    }

    public static void addHeader(PDPageContentStream contentStream,
                                 PDType0Font font,
                                 float pageHeight,
                                 float pageWidth,
                                 int pageNumber) throws IOException {
        drawText(contentStream, font, "문서 제목 또는 회사 이름", 20, pageHeight - 20, 12);
        drawText(contentStream, font, "페이지 : " + pageNumber, pageWidth - 100, pageHeight - 20, 12);
        drawLine(contentStream, 0, pageHeight - PdfConstants.HEADER_HEIGHT, pageWidth, pageHeight - PdfConstants.HEADER_HEIGHT);
    }

    public static void addFooter(PDPageContentStream contentStream,
                                 PDType0Font font,
                                 float pageWidth) throws IOException {
        drawText(contentStream, font, "페이지 번호 또는 회사 정보", 20, PdfConstants.VERTICAL_MARGIN + 20, 12);
        drawLine(contentStream, 0, PdfConstants.FOOTER_HEIGHT, pageWidth, PdfConstants.FOOTER_HEIGHT);
    }

    public static void addSectionDividers(PDPageContentStream contentStream,
                                          float pageWidth,
                                          float pageHeight) throws IOException {
        float sectionWidth = pageWidth / 3;
        drawLine(contentStream, sectionWidth, PdfConstants.FOOTER_HEIGHT, sectionWidth, pageHeight - PdfConstants.HEADER_HEIGHT);
        drawLine(contentStream, 2 * sectionWidth, PdfConstants.FOOTER_HEIGHT, 2 * sectionWidth, pageHeight - PdfConstants.HEADER_HEIGHT);
    }

    public static void addImage(PDPageContentStream contentStream,
                                MultipartFile imageFile,
                                PDDocument document,
                                float sectionWidth,
                                float sectionHeight) throws IOException {
        float availableHeight = sectionHeight - (2 * PdfConstants.VERTICAL_MARGIN);
        float availableWidth = sectionWidth - 10;

        PDImageXObject image = PDImageXObject.createFromByteArray(document, imageFile.getBytes(), imageFile.getOriginalFilename());
        float originalAspectRatio = image.getWidth() / (float) image.getHeight();

        float imageWidth = availableWidth;
        float imageHeight = imageWidth / originalAspectRatio;

        if (imageHeight > availableHeight) {
            imageHeight = availableHeight;
            imageWidth = imageHeight * originalAspectRatio;
        }

        float xPosition = 5 + (sectionWidth - imageWidth) / 2;
        float yPosition = PdfConstants.FOOTER_HEIGHT + PdfConstants.VERTICAL_MARGIN + (availableHeight - imageHeight) / 2;

        contentStream.drawImage(image, xPosition, yPosition, imageWidth, imageHeight);
    }

    public static void addTextToSections(PDPageContentStream contentStream,
                                         PDType0Font font,
                                         float sectionWidth,
                                         float pageHeight,
                                         ProposalComments comments) throws IOException {
        drawText(contentStream, font, "화면 및 기능 설명", sectionWidth + 20, pageHeight - 60, 14);

        drawText(contentStream, font, comments.getContent()
                != null
                ? comments.getContent() : "기능 설명이 없습니다.", sectionWidth + 20, pageHeight - 80, 12);

        drawText(contentStream, font, "수정 요청사항", 2 * sectionWidth + 20, pageHeight - 60, 14);

        drawText(contentStream, font, comments.getModificationRequirements()
                != null
                ? comments.getModificationRequirements() : "수정 요청사항이 없습니다.", 2 * sectionWidth + 20, pageHeight - 80, 12);
    }

}

