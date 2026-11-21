package com.kharid.dto;

import jakarta.validation.constraints.NotBlank;

public record SavedRequest(@NotBlank String productId, @NotBlank String name, Integer price,
                           String marketplace, String url) {
}
