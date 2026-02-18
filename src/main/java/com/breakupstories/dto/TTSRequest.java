package com.breakupstories.dto;

public class TTSRequest {
    private String text;
    private String language;
    private String gender;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
