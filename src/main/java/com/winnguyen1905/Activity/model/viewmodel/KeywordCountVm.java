package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordCountVm implements AbstractModel {
  private String keyword;
  private Long count;
}
