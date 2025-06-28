package com.winnguyen1905.activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class RestResponse<T> implements AbstractModel {
    private T data;
    private String error;
    private Object message;
    private Integer statusCode;
}
