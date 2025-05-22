package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(value = Include.NON_NULL)
public record PagedResponse<T>(
    long maxPageItems,
    long page,
    long size,
    List<T> results,
    long totalElements,
    long totalPages) implements AbstractModel {
  @Builder
  public PagedResponse(
      long maxPageItems,
      long page,
      long size,
      List<T> results,
      long totalElements,
      long totalPages) {
    this.maxPageItems = maxPageItems;
    this.page = page;
    this.size = size;
    this.results = results;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
  }
}
