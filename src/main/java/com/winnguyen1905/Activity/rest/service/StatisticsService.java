package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.Activity.model.viewmodel.StatisticsVm;

public interface StatisticsService {
    // Get all statistics without filtering
    StatisticsVm getActivityStatistics(TAccountRequest accountRequest);
    
    // Get statistics with filtering options
    StatisticsVm getFilteredActivityStatistics(TAccountRequest accountRequest, StatisticsFilterDto filterDto);
}
