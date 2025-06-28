package com.winnguyen1905.activity.rest.service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.StatisticsVm;

public interface StatisticsService {
    // Get all statistics without filtering
    StatisticsVm getActivityStatistics(TAccountRequest accountRequest);
    
    // Get statistics with filtering options
    StatisticsVm getFilteredActivityStatistics(TAccountRequest accountRequest, StatisticsFilterDto filterDto);
}
