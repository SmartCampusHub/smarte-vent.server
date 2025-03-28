package com.winnguyen1905.Activity.common.annotation;

import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

@Builder
public record TAccountRequest(
    Long id,
    String username,
    AccountRole role,
    UUID socketClientId) implements AbstractModel {
}
