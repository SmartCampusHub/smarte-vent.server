package com.winnguyen1905.activity.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility class providing helper methods for date and time operations
 * Particularly focused on time period calculations for statistics
 */
public class DateTimeUtils {
    
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    
    /**
     * Get the start of the current day as Instant
     * @return Instant representing the start of the current day
     */
    public static Instant getStartOfCurrentDay() {
        LocalDate today = LocalDate.now();
        return today.atStartOfDay(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the end of the current day as Instant
     * @return Instant representing the end of the current day
     */
    public static Instant getEndOfCurrentDay() {
        LocalDate today = LocalDate.now();
        return today.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the start of the current week as Instant
     * Assumes week starts on Monday
     * @return Instant representing the start of the current week
     */
    public static Instant getStartOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return startOfWeek.atStartOfDay(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the end of the current week as Instant
     * Assumes week ends on Sunday
     * @return Instant representing the end of the current week
     */
    public static Instant getEndOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return endOfWeek.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the start of the current month as Instant
     * @return Instant representing the start of the current month
     */
    public static Instant getStartOfCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        return firstDayOfMonth.atStartOfDay(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the end of the current month as Instant
     * @return Instant representing the end of the current month
     */
    public static Instant getEndOfCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the start of the current quarter as Instant
     * @return Instant representing the start of the current quarter
     */
    public static Instant getStartOfCurrentQuarter() {
        LocalDate today = LocalDate.now();
        int currentQuarter = today.get(IsoFields.QUARTER_OF_YEAR);
        Month firstMonthOfQuarter = switch (currentQuarter) {
            case 1 -> Month.JANUARY;
            case 2 -> Month.APRIL;
            case 3 -> Month.JULY;
            case 4 -> Month.OCTOBER;
            default -> Month.JANUARY;
        };
        
        LocalDate firstDayOfQuarter = LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
        return firstDayOfQuarter.atStartOfDay(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the end of the current quarter as Instant
     * @return Instant representing the end of the current quarter
     */
    public static Instant getEndOfCurrentQuarter() {
        LocalDate today = LocalDate.now();
        int currentQuarter = today.get(IsoFields.QUARTER_OF_YEAR);
        Month lastMonthOfQuarter = switch (currentQuarter) {
            case 1 -> Month.MARCH;
            case 2 -> Month.JUNE;
            case 3 -> Month.SEPTEMBER;
            case 4 -> Month.DECEMBER;
            default -> Month.MARCH;
        };
        
        YearMonth yearMonth = YearMonth.of(today.getYear(), lastMonthOfQuarter);
        LocalDate lastDayOfQuarter = yearMonth.atEndOfMonth();
        return lastDayOfQuarter.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the start of the current year as Instant
     * @return Instant representing the start of the current year
     */
    public static Instant getStartOfCurrentYear() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfYear = LocalDate.of(today.getYear(), 1, 1);
        return firstDayOfYear.atStartOfDay(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Get the end of the current year as Instant
     * @return Instant representing the end of the current year
     */
    public static Instant getEndOfCurrentYear() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfYear = LocalDate.of(today.getYear(), 12, 31);
        return lastDayOfYear.atTime(LocalTime.MAX).atZone(DEFAULT_ZONE).toInstant();
    }
    
    /**
     * Convert an Instant to LocalDateTime
     * @param instant the Instant to convert
     * @return LocalDateTime representing the same point in time
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, DEFAULT_ZONE) : null;
    }
    
    /**
     * Convert a LocalDateTime to Instant
     * @param localDateTime the LocalDateTime to convert
     * @return Instant representing the same point in time
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(DEFAULT_ZONE).toInstant() : null;
    }
    
    /**
     * Check if an Instant is within a date range
     * @param instant the Instant to check
     * @param startDate the start of the range
     * @param endDate the end of the range
     * @return true if the Instant is within the range, false otherwise
     */
    public static boolean isWithinRange(Instant instant, Instant startDate, Instant endDate) {
        if (instant == null || startDate == null || endDate == null) {
            return false;
        }
        return !instant.isBefore(startDate) && !instant.isAfter(endDate);
    }
}
