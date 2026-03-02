package com.compassed.compassed_api.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateResponse {
    
    private Long paymentId;
    private String transactionId;
    private String paymentUrl;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
}
