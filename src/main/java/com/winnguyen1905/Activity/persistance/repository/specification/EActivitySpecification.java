package com.winnguyen1905.Activity.persistance.repository.specification;

import com.winnguyen1905.Activity.model.dto.ActivitySearchRequest;
import com.winnguyen1905.Activity.persistance.entity.EActivity;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EActivitySpecification {

  public static Specification<EActivity> filterBy(ActivitySearchRequest searchRequest) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchRequest.activityName() != null && !searchRequest.activityName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("activityName")),
            "%" + searchRequest.activityName().toLowerCase() + "%"));
      }

      if (searchRequest.activityCategory() != null) {
        predicates.add(criteriaBuilder.equal(root.get("activityCategory"), searchRequest.activityCategory()));
      }

      if (searchRequest.activityStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("activityStatus"), searchRequest.activityStatus()));
      }

      if (searchRequest.organizationId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("organization").get("id"), searchRequest.organizationId()));
      }

      if (searchRequest.startDateFrom() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), searchRequest.startDateFrom()));
      }

      if (searchRequest.startDateTo() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), searchRequest.startDateTo()));
      }

      if (searchRequest.endDateFrom() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), searchRequest.endDateFrom()));
      }

      if (searchRequest.endDateTo() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), searchRequest.endDateTo()));
      }

      if (searchRequest.minAttendanceScoreUnit() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("attendanceScoreUnit"),
            searchRequest.minAttendanceScoreUnit()));
      }

      if (searchRequest.maxAttendanceScoreUnit() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("attendanceScoreUnit"), searchRequest.maxAttendanceScoreUnit()));
      }

      if (searchRequest.minCapacityLimit() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), searchRequest.minCapacityLimit()));
      }

      if (searchRequest.maxCapacityLimit() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("capacity"), searchRequest.maxCapacityLimit()));
      }

      if (searchRequest.activityVenue() != null && !searchRequest.activityVenue().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("activityVenue")),
            "%" + searchRequest.activityVenue().toLowerCase() + "%"));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
