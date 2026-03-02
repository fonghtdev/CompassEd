package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.request.PaymentCallbackRequest;
import com.compassed.compassed_api.api.dto.request.PaymentCreateRequest;
import com.compassed.compassed_api.api.dto.response.PaymentCallbackResponse;
import com.compassed.compassed_api.api.dto.response.PaymentCreateResponse;

public interface IPaymentService {
    
    PaymentCreateResponse createPayment(PaymentCreateRequest request);
    
    PaymentCallbackResponse handleCallback(PaymentCallbackRequest request);
}
