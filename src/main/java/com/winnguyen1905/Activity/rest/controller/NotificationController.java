package com.winnguyen1905.Activity.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.viewmodel.NotificationVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.rest.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("")
  public ResponseEntity<PagedResponse<NotificationVm>> getNotifications(@AccountRequest TAccountRequest accountRequest,
      Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(accountRequest, pageable));
  }

  @GetMapping("/send")
  public ResponseEntity<Void> sendNotification(@AccountRequest TAccountRequest accountRequest) {
    notificationService.sendNotification(accountRequest, null);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<Void> postMethodName(@PathVariable Long id, @AccountRequest TAccountRequest accountRequest) {
    notificationService.readNotification(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/delete")
  public ResponseEntity<Void> deleteNotification(@PathVariable Long id, @AccountRequest TAccountRequest accountRequest) {
    notificationService.deleteNotification(accountRequest, id);
    return ResponseEntity.noContent().build();
  } 

}
