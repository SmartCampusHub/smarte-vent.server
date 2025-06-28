package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCategoryVm implements AbstractModel {
    private Long id;
    private String name;
    private String description;
    private String status;
}
