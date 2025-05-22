package com.winnguyen1905.Activity.persistance.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.Activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.Activity.persistance.entity.EOrganization;

import jakarta.persistence.criteria.Predicate;

public class OrganizationSpecification {

  public static Specification<EOrganization> search(OrganizationSearchRequest request) {
    return (root, query, cb) -> {
      Predicate predicate = cb.conjunction();

      if (request.name() != null && !request.name().isEmpty()) {
        predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + request.name().toLowerCase() + "%"));
      }

      if (request.organizationType() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("type"), request.organizationType()));
      }

      return predicate;
    };
  }
}
