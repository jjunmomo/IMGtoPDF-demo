package com.example.demo.pdf.dto;

public class ProposalComments {

    private String content;  // 기능 설명

    private String modificationRequirements;  // 수정 요청사항

    public ProposalComments() {
    }

    public ProposalComments(String content, String modificationRequirements) {
        this.content = content;
        this.modificationRequirements = modificationRequirements;
    }

    // Getter 및 Setter 메소드
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModificationRequirements() {
        return modificationRequirements;
    }

    public void setModificationRequirements(String modificationRequirements) {
        this.modificationRequirements = modificationRequirements;
    }
}
