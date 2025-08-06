package com.assembliestore.api.module.payment.domain.port;

import com.assembliestore.api.module.payment.domain.entity.PaymentRequest;

public interface PaymentPort {
    String createPayment(PaymentRequest request);
}
