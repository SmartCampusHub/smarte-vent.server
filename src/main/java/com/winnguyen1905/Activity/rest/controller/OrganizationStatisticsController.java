package com.winnguyen1905.activity.rest.controller;

import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.OrganizationStatisticsVm;
import com.winnguyen1905.activity.rest.service.OrganizationStatisticsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization-statistics")
@Api(value = "Organization Statistics Controller", description = "APIs for organization statistics")
@RequiredArgsConstructor
public class OrganizationStatisticsController {

  private final OrganizationStatisticsService organizationStatisticsService;

  @GetMapping("/organization/{organizationId}")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/filter")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get filtered statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getFilteredOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId,
      @RequestParam(required = false) String timePeriod,
      @RequestParam(required = false) String activityType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

    StatisticsFilterDto filter = new StatisticsFilterDto();
    if (timePeriod != null) {
      filter.setTimePeriod(com.winnguyen1905.activity.common.constant.TimePeriod.valueOf(timePeriod.toUpperCase()));
    }
    if (activityType != null) {
      filter.setActivityType(
          com.winnguyen1905.activity.common.constant.ActivityCategory.valueOf(activityType.toUpperCase()));
    }
    if (status != null) {
      filter.setStatus(com.winnguyen1905.activity.common.constant.ActivityStatus.valueOf(status.toUpperCase()));
    }
    filter.setStartDate(startDate);
    filter.setEndDate(endDate);

    return ResponseEntity.ok(organizationStatisticsService.getFilteredOrganizationStatistics(organizationId, filter));
  }

  @GetMapping("/organization/{organizationId}/daily")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get daily statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getDailyOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getDailyOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/weekly")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get weekly statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getWeeklyOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getWeeklyOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/monthly")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get monthly statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getMonthlyOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getMonthlyOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/quarterly")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get quarterly statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getQuarterlyOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getQuarterlyOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/yearly")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get yearly statistics for a specific organization")
  public ResponseEntity<OrganizationStatisticsVm> getYearlyOrganizationStatistics(
      @PathVariable("organizationId") Long organizationId) {
    return ResponseEntity.ok(organizationStatisticsService.getYearlyOrganizationStatistics(organizationId));
  }

  @GetMapping("/organization/{organizationId}/date-range")
  // @PreAuthorize("hasRole('ROLE_ADMIN') or
  // @securityService.isOrganizationMember(#organizationId)")
  @ApiOperation(value = "Get statistics for a specific organization within a custom date range")
  public ResponseEntity<OrganizationStatisticsVm> getOrganizationStatisticsInDateRange(
      @PathVariable("organizationId") Long organizationId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
    return ResponseEntity
        .ok(organizationStatisticsService.getOrganizationStatisticsInDateRange(organizationId, startDate, endDate));
  }
}
