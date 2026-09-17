package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoContactSyncResponse {
    private boolean success;
    private String message;
    private String startDate;
    private int targetDays;
    private String exNickname;
    private String motivation;
    private String lastCheckInDate;
    private int sosCount;
    private List<NoContactSyncRequest.SyncMoodCheckIn> checkIns;
    private List<NoContactSyncRequest.SyncResistedMessage> resistedMessages;
}
