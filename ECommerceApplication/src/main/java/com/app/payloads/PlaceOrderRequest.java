package com.app.payloads;

import com.app.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotNull
    private Long cartId;

    @NotNull
    private PaymentMethod paymentMethod;
}