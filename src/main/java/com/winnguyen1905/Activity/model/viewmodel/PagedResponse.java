package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public record PagedResponse<T>(
    long maxPageItems,
    long page,
    long size,
    List<T> results,
    long totalElements,
    long totalPages) implements AbstractModel {
}
