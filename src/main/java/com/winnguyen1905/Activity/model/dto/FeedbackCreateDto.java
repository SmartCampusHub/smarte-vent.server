package com.winnguyen1905.activity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreateDto  implements AbstractModel{
    
    @NotNull(message = "Activity ID is required")
    private Long activityId;
    
    @NotNull(message = "Participation ID is required")
    private Long participationId;
    
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 10, message = "Rating must be at most 10")
    @NotNull(message = "Rating is required")
    private Double rating;
    
    private String feedbackDescription;
}
