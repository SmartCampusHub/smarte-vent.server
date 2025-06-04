package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

@Builder
public record CheckJoinedActivityVm(Boolean isJoined) implements AbstractModel {
    @Builder
    public CheckJoinedActivityVm(Boolean isJoined) {
        this.isJoined = isJoined;
    }
}
