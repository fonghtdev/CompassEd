package com.compassed.compassed_api.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackResponse {
    
    private String transactionId;
    private String status;
    private String message;
    private Long subscriptionId;
    private Boolean placementUnlocked;
}
