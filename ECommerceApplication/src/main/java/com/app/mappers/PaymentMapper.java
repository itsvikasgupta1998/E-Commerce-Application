package com.app.mappers;

import com.app.entites.Payment;
import com.app.payloads.PaymentResponse;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);
}
