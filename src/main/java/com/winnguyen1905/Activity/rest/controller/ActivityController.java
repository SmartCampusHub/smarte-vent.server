package com.winnguyen1905.Activity.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("activity")
public class ActivityController {
  @GetMapping("/test")
  public String getMethodName(@RequestParam String param) {
      return new String();
  }
}
