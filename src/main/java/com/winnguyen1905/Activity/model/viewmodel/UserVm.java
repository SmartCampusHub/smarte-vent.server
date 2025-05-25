package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

public record UserVm(
        UUID id,
        String email,
        String username,
        String avatarUrl,
        List<String> roles) implements AbstractModel {
}
