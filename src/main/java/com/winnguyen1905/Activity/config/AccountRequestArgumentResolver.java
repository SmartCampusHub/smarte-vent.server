package com.winnguyen1905.activity.config;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.AccountRole;

@Component
public class AccountRequestArgumentResolver implements HandlerMethodArgumentResolver {

  public static enum AccountRequestArgument {
    ID("sub"), USERNAME("username"), ROLE("role");

    String value;

    AccountRequestArgument(String value) {
      this.value = value;
    }
  };

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AccountRequest.class) &&
        parameter.getParameterType().equals(TAccountRequest.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      throw new IllegalStateException("No valid JWT authentication found");
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();

    Long id = Long.valueOf(jwt.getClaimAsString(AccountRequestArgument.ID.value));
    String username = jwt.getClaimAsString(AccountRequestArgument.USERNAME.value);
    AccountRole role = AccountRole.valueOf(jwt.getClaimAsString(AccountRequestArgument.ROLE.value));

    return TAccountRequest.builder()
        .id(id)
        .username(username)
        .role(role).build();
  }
}
