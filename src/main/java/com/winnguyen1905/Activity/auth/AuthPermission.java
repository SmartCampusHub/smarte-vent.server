package com.winnguyen1905.activity.auth;

import lombok.Builder;

public record AuthPermission(String name, String code, String apiPath, String method, String module) {}
