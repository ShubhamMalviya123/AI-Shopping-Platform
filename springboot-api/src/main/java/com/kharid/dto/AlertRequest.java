package com.kharid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertRequest(@NotBlank String productId, @NotBlank String productName,
                           @NotNull @Positive Integer targetPrice, Integer currentPrice) {
}
