package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.NotificationDto;
import com.winnguyen1905.Activity.model.viewmodel.NotificationVm;
import com.winnguyen1905.Activity.persistance.repository.NotificationRepository;
import com.winnguyen1905.Activity.rest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public void createNotification(TAccountRequest accountRequest, NotificationDto notificationDto) {
        // TODO: Implement create notification logic
    }

    @Override
    public void deleteNotification(TAccountRequest accountRequest, Long id) {
        // TODO: Implement delete notification logic
    }

    @Override
    public NotificationVm getNotificationById(Long id) {
        // TODO: Implement get notification by id logic
        return null;
    }

    @Override
    public List<NotificationVm> getNotificationsByReceiverId(Long receiverId) {
        // TODO: Implement get notifications by receiver id logic
        return null;
    }

    @Override
    public List<NotificationVm> getNotificationsBySenderId(Long senderId) {
        // TODO: Implement get notifications by sender id logic
        return null;
    }
}
