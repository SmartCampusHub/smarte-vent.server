package com.winnguyen1905.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for typing indicators.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDto implements AbstractModel {
    private Long userId;
    private String userName;
    private Long receiverId;
    private Long activityId; // For activity chat rooms
    private Boolean isTyping;
} 
