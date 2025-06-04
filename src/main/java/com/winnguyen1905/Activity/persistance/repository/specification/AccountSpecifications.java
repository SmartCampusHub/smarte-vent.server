package com.winnguyen1905.Activity.persistance.repository.specification;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.MajorType;
import com.winnguyen1905.Activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecifications {

  public static Specification<EAccountCredentials> hasFullName(String fullName) {
    return (root, _, cb) -> fullName == null ? null
        : cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
  }

  public static Specification<EAccountCredentials> hasEmail(String email) {
    return (root, _, cb) -> email == null ? null
        : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
  }

  public static Specification<EAccountCredentials> hasPhone(String phone) {
    return (root, _, cb) -> phone == null ? null : cb.like(root.get("phone"), "%" + phone + "%");
  }

  public static Specification<EAccountCredentials> hasRole(AccountRole role) {
    return (root, _, cb) -> role == null ? null : cb.equal(root.get("role"), role);
  }

  public static Specification<EAccountCredentials> hasMajor(MajorType major) {
    return (root, _, cb) -> major == null ? null : cb.equal(root.get("major"), major);
  }

  public static Specification<EAccountCredentials> isActive(Boolean isActive) {
    return (root, _, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<EAccountCredentials> hasIdentifyCode(String identifyCode) {
    return (root, _, cb) -> identifyCode == null ? null : cb.like(root.get("identifyCode"), "%" + identifyCode + "%");
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
