package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.OrganizationType;

public record OrganizationDto(
    String id,
    String name,
    String email,
    String phone,
    String position,
    OrganizationType type) {
}
