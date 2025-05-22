package com.winnguyen1905.Activity.persistance.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EParticipationDetailSpecification {

  public static Specification<EParticipationDetail> filterBy(ParticipationSearchParams searchDTO) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchDTO.participantId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participant").get("id"), searchDTO.participantId()));
      }

      if (searchDTO.participantName() != null && !searchDTO.participantName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            root.get("participant").get("fullName"),
            "%" + searchDTO.participantName() + "%"));
      }

      if (searchDTO.studentCode() != null) {
        predicates.add(criteriaBuilder.equal(
            root.get("participant").get("studentCode"),
            searchDTO.studentCode()));
      }

      if (searchDTO.participantName() != null && !searchDTO.participantName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            root.get("participant").get("fullName"),
            "%" + searchDTO.participantName() + "%"));
      }

      if (searchDTO.participationStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participationStatus"), searchDTO.participationStatus()));
      }

      if (searchDTO.participationRole() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participationRole"), searchDTO.participationRole()));
      }

      if (searchDTO.registeredAfter() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registeredAt"), searchDTO.registeredAfter()));
      }

      if (searchDTO.registeredBefore() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registeredAt"), searchDTO.registeredBefore()));
      }

      // Combine predicates using AND logic
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
