package com.winnguyen1905.Activity.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto implements AbstractModel {
    @NotNull(message = "Feedback ID is required")
    private Long feedbackId;
    
    @NotBlank(message = "Response message is required")
    private String response;
} 
