package com.winnguyen1905.activity.persistance.repository.specification;

import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.model.dto.ActivitySearchRequest;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EActivitySpecification {

  public static Specification<EActivity> filterBy(ActivitySearchRequest searchRequest) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchRequest.getActivityName() != null && !searchRequest.getActivityName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("activityName")),
            "%" + searchRequest.getActivityName().toLowerCase() + "%"));
      }

      if (searchRequest.getActivityCategory() != null) {
        predicates.add(criteriaBuilder.equal(root.get("activityCategory"), searchRequest.getActivityCategory()));
      }

      if (searchRequest.getActivityStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getActivityStatus()));
      }

      if (searchRequest.getOrganizationName() != null) {
        predicates.add(criteriaBuilder.like(root.get("organization").get("name"), "%" + searchRequest.getOrganizationName() + "%"));
      }

      if (searchRequest.getStartDateFrom() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), searchRequest.getStartDateFrom()));
      }

      if (searchRequest.getStartDateTo() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), searchRequest.getStartDateTo()));
      }

      if (searchRequest.getEndDateFrom() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), searchRequest.getEndDateFrom()));
      }

      if (searchRequest.getEndDateTo() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), searchRequest.getEndDateTo()));
      }

      if (searchRequest.getMinAttendanceScoreUnit() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("attendanceScoreUnit"),
            searchRequest.getMinAttendanceScoreUnit()));
      }

      if (searchRequest.getMaxAttendanceScoreUnit() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("attendanceScoreUnit"), searchRequest.getMaxAttendanceScoreUnit()));
      }

      if (searchRequest.getMinCapacityLimit() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("capacityLimit"), searchRequest.getMinCapacityLimit()));
      }

      if (searchRequest.getMaxCapacityLimit() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("capacityLimit"), searchRequest.getMaxCapacityLimit()));
      }

      if (searchRequest.getIsApproved() != null) {
        predicates.add(criteriaBuilder.equal(root.get("isApproved"), searchRequest.getIsApproved()));
      }

      if (searchRequest.getActivityVenue() != null && !searchRequest.getActivityVenue().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("activityVenue")),
            "%" + searchRequest.getActivityVenue().toLowerCase() + "%"));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
