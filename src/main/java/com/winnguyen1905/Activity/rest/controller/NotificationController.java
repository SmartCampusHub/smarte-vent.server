package com.winnguyen1905.Activity.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.viewmodel.NotificationVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.rest.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<PagedResponse<NotificationVm>> getMethodName(TAccountRequest accountRequest,
      Pageable pageable) {
    return ResponseEntity.ok(notificationService.getNotifications(accountRequest, pageable));
  }

  @GetMapping("/send")
  public ResponseEntity<Void> sendNotification(TAccountRequest accountRequest) {
    notificationService.sendNotifaction(accountRequest, null);
    return ResponseEntity.ok().build();
  }
}
