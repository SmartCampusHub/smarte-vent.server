package com.winnguyen1905.activity.websocket.dto;

import com.winnguyen1905.activity.rest.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for user status updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto implements AbstractModel {
    private Long userId;
    private String userName;
    private UserStatus status;
    private Instant lastSeen;
    private String statusMessage;
    
    public enum UserStatus {
        ONLINE,
        AWAY,
        BUSY,
        OFFLINE
    }
} 
