package com.winnguyen1905.Activity.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("activity")
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/create")
  public ResponseEntity<Void> createActivity(@AccountRequest TAccountRequest accountRequest, ActivityDto activityDto) {
    this.activityService.createActivity(accountRequest, activityDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}
