package com.winnguyen1905.activity.rest.service;

import java.time.Instant;

import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.OrganizationStatisticsVm;

public interface OrganizationStatisticsService {
    /**
     * Get organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing statistics for the organization
     */
    OrganizationStatisticsVm getOrganizationStatistics(Long organizationId);
    
    /**
     * Get filtered organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @param filter The filter to apply to the statistics
     * @return OrganizationStatisticsVm containing filtered statistics for the organization
     */
    OrganizationStatisticsVm getFilteredOrganizationStatistics(Long organizationId, StatisticsFilterDto filter);
    
    /**
     * Get daily organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing daily statistics for the organization
     */
    OrganizationStatisticsVm getDailyOrganizationStatistics(Long organizationId);
    
    /**
     * Get weekly organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing weekly statistics for the organization
     */
    OrganizationStatisticsVm getWeeklyOrganizationStatistics(Long organizationId);
    
    /**
     * Get monthly organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing monthly statistics for the organization
     */
    OrganizationStatisticsVm getMonthlyOrganizationStatistics(Long organizationId);
    
    /**
     * Get quarterly organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing quarterly statistics for the organization
     */
    OrganizationStatisticsVm getQuarterlyOrganizationStatistics(Long organizationId);
    
    /**
     * Get yearly organization statistics for a specific organization
     * @param organizationId The ID of the organization
     * @return OrganizationStatisticsVm containing yearly statistics for the organization
     */
    OrganizationStatisticsVm getYearlyOrganizationStatistics(Long organizationId);
    
    /**
     * Get organization statistics for a specific organization within a custom date range
     * @param organizationId The ID of the organization
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return OrganizationStatisticsVm containing statistics for the organization in the date range
     */
    OrganizationStatisticsVm getOrganizationStatisticsInDateRange(Long organizationId, Instant startDate, Instant endDate);
}
