package com.breakupstories.dto;

public class TTSResponse {
    private String audioData; // Base64 encoded audio data
    private String status;
    private String error;

    public TTSResponse(String audioData, String status) {
        this.audioData = audioData;
        this.status = status;
    }

    public TTSResponse(String status, String error, boolean isError) {
        this.status = status;
        this.error = error;
    }

    public String getAudioData() { return audioData; }
    public void setAudioData(String audioData) { this.audioData = audioData; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
