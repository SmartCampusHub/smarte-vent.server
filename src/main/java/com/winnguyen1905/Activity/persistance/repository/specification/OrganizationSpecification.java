package com.winnguyen1905.activity.persistance.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.activity.persistance.entity.EOrganization;

import jakarta.persistence.criteria.Predicate;

public class OrganizationSpecification {

  public static Specification<EOrganization> search(OrganizationSearchRequest request) {
    return (root, query, cb) -> {
      Predicate predicate = cb.conjunction();

      if (request.getName() != null && !request.getName().isEmpty()) {
        predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
      }

      if (request.getOrganizationType() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("type"), request.getOrganizationType()));
      }

      return predicate;
    };
  }
}
