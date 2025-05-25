package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

public record ActivityCategoryVm(
    Long id,
    String name,
    String description,
    String status) implements AbstractModel{
}
