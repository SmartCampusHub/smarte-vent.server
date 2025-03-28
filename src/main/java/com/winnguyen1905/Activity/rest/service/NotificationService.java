package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.NotificationDto;
import com.winnguyen1905.Activity.model.viewmodel.NotificationVm;

import java.util.List;

public interface NotificationService {
    void createNotification(TAccountRequest accountRequest, NotificationDto notificationDto);
    void deleteNotification(TAccountRequest accountRequest, Long id);
    NotificationVm getNotificationById(Long id);
    List<NotificationVm> getNotificationsByReceiverId(Long receiverId);
    List<NotificationVm> getNotificationsBySenderId(Long senderId);
}
