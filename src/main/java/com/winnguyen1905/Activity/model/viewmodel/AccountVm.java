package com.winnguyen1905.Activity.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

@Builder
public record AccountVm(
    Long id,
    String name,
    String email,
    String phone,
    String studentCode,
    AccountRole accountRole) implements AbstractModel {
}
