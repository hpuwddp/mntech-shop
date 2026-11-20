package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReceivePrizeResponse {
    private String recordId;
    private String prizeName;
    private Integer prizeLevel;
    private String prizeLevelDesc;
    private String status;  // RECEIVED
    private String receiveAddress;
    private LocalDateTime receiveTime;
    private String message;
}