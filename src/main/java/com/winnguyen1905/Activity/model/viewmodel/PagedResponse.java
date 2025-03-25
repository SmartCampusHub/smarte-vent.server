package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PagedResponse<T> implements AbstractModel {
  private int maxPageItems;

  private int page;

  private int size;

  private List<T> results;

  private int totalElements;

  private int totalPages;
}
