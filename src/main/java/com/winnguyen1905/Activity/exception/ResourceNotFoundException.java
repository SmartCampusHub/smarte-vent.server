package com.winnguyen1905.Activity.exception;

public class ResourceNotFoundException extends BaseException {
  public ResourceNotFoundException(String message, Integer code, Object error) {
    super(message, code, error);
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
