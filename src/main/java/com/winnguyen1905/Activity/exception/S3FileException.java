package com.winnguyen1905.Activity.exception;

public class S3FileException extends BaseException {
  public S3FileException(String message, int code, Object error) {
    super(message, code, error);
  }

  public S3FileException(String message) {
    super(message, 500);
  }
}
