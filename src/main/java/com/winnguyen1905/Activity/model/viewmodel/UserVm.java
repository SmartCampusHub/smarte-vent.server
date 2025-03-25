package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;
import java.util.UUID;

public record UserVm(
    UUID id,
    String email,
    String username,
    String avatarUrl,
    List<String> roles) {
}
