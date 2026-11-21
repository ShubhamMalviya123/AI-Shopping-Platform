package com.kharid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequest(@NotBlank @Size(min = 3, max = 300) String query) {
}
