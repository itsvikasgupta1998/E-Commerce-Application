package com.app.payloads;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentFailureRequest {

    @NotBlank(
            message = "Failure reason is required"
    )
    private String failureReason;
}