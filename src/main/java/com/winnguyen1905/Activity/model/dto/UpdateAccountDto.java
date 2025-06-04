package com.winnguyen1905.Activity.model.dto;

public record UpdateAccountDto(
        Long id,
        String phone,
        String email) implements AbstractModel {

}
