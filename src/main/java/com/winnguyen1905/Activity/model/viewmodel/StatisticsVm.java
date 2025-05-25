package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;
import java.util.Map;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVm implements AbstractModel {
  private Long totalActivities;
  private Long totalParticipants;
  private Long activitiesLastMonth;
  private Long activitiesLastWeek;
  private Double averageRating;
  private Map<String, Long> activitiesByCategory;

  // New statistics
  private Long totalReviews;
  private Map<Long, Double> averageScoreByActivity;
  private List<KeywordCountVm> topKeywords;

  // @Data
  // @Builder
  // @NoArgsConstructor
  // @AllArgsConstructor
  // public static class KeywordCountVm implements AbstractModel {
  //   private String keyword;
  //   private Long count;
  // }

}
