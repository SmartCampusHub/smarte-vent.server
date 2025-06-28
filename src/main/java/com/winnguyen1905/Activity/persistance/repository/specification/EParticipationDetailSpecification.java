package com.winnguyen1905.activity.persistance.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EParticipationDetailSpecification {

  public static Specification<EParticipationDetail> filterBy(ParticipationSearchParams searchDTO) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchDTO.getParticipantId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participant").get("id"), searchDTO.getParticipantId()));
      }

      if (searchDTO.getParticipantName() != null && !searchDTO.getParticipantName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            root.get("participant").get("fullName"),
            "%" + searchDTO.getParticipantName() + "%"));
      }

      if (searchDTO.getIdentifyCode() != null) {
        predicates.add(criteriaBuilder.like(
            root.get("participant").get("identifyCode"),
            "%" + searchDTO.getIdentifyCode() + "%"));
      }
      if (searchDTO.getParticipationStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participationStatus"), searchDTO.getParticipationStatus()));
      }

      if (searchDTO.getParticipationRole() != null) {
        predicates.add(criteriaBuilder.equal(root.get("participationRole"), searchDTO.getParticipationRole()));
      }

      if (searchDTO.getRegisteredAfter() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registeredAt"), searchDTO.getRegisteredAfter()));
      }

      if (searchDTO.getRegisteredBefore() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registeredAt"), searchDTO.getRegisteredBefore()));
      }

      // Combine predicates using AND logic
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
