package com.winnguyen1905.activity.model.viewmodel;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class PagedResponse<T> implements AbstractModel {
    private long maxPageItems;
    private long page;
    private long size;
    private List<T> results;
    private long totalElements;
    private long totalPages;
}
