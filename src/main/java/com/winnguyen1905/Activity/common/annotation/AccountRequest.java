package com.winnguyen1905.Activity.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "@fetchUser.apply(#this)", errorOnInvalidType=true)
public @interface AccountRequest {}
