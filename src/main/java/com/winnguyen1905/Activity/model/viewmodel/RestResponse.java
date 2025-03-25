package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.*;

@Builder
public record RestResponse<T>(
  T data,
  String error,
  Object message,
  Integer statusCode
) implements AbstractModel {}
