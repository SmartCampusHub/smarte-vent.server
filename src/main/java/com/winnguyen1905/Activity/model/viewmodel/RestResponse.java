package com.winnguyen1905.Activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.*;

@JsonInclude(value = com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public record RestResponse<T>(
  T data,
  String error,
  Object message,
  Integer statusCode
) implements AbstractModel {
  @Builder
  public RestResponse(
    T data,
    String error,
    Object message,
    Integer statusCode
  ) {
    this.data = data;
    this.error = error;
    this.message = message;
    this.statusCode = statusCode;
  }
}
