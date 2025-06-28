package com.winnguyen1905.activity.exception;

public class BadRequestException extends BaseException {
  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, int code) {
    super(message, code);
  }
}
