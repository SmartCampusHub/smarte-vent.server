package com.winnguyen1905.Activity.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("test")
public class Test {
  @GetMapping("/hahaha")
  public String getMethodName(@AccountRequest TAccountRequest accountRequest) {
      return new String();
  }
  
}
