package com.winnguyen1905.Activity.model.dto;

import java.util.UUID;

public record VerifyCodeDto(String code, UUID userId) {}
