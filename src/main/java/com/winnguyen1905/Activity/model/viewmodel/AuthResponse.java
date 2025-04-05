package com.winnguyen1905.Activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

@Builder
public record AuthResponse(String accessToken, AccountVm account, @JsonIgnore String refreshToken) implements AbstractModel {}
