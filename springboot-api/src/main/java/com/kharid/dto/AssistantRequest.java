package com.kharid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequest(@NotBlank @Size(min = 3, max = 500) String message) {
}
