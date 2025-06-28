package com.winnguyen1905.activity.persistance.repository.specification;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import com.winnguyen1905.activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecifications {

  public static Specification<EAccountCredentials> hasFullName(String fullName) {
    return (root, query, cb) -> fullName == null ? null
        : cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
  }

  public static Specification<EAccountCredentials> hasEmail(String email) {
    return (root, query, cb) -> email == null ? null
        : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
  }

  public static Specification<EAccountCredentials> hasPhone(String phone) {
    return (root, query, cb) -> phone == null ? null : cb.like(root.get("phone"), "%" + phone + "%");
  }

  public static Specification<EAccountCredentials> hasRole(AccountRole role) {
    return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
  }

  public static Specification<EAccountCredentials> hasMajor(MajorType major) {
    return (root, query, cb) -> major == null ? null : cb.equal(root.get("major"), major);
  }

  public static Specification<EAccountCredentials> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<EAccountCredentials> hasIdentifyCode(String identifyCode) {
    return (root, query, cb) -> identifyCode == null ? null : cb.like(root.get("identifyCode"), "%" + identifyCode + "%");
  }

  public static Specification<EAccountCredentials> withCriteria(AccountSearchCriteria criteria) {
    return Specification.where(hasFullName(criteria.getFullName()))
        .and(hasEmail(criteria.getEmail()))
        .and(hasPhone(criteria.getPhone()))
        .and(hasIdentifyCode(criteria.getIdentifyCode()))
        .and(hasRole(criteria.getRole()))
        .and(hasMajor(criteria.getMajor()))
        .and(isActive(criteria.getIsActive()));
  }
}
