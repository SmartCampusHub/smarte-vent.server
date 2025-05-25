package com.winnguyen1905.Activity.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

public record UserVerificationVm(UUID id, String verificationCode)  implements AbstractModel  {}
