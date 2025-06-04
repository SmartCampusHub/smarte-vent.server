package com.winnguyen1905.Activity.persistance.repository.TriggerRepo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.persistance.entity.EFeedback;

public interface FeedbackRepository   {

  @Query("SELECT AVG(f.rating) FROM EFeedback f")
  Double getAverageRating();

  @Query("SELECT COUNT(f) FROM EFeedback f")
  Long countTotalReviews();

  @Query("SELECT f.activity.id, AVG(f.rating) FROM EFeedback f GROUP BY f.activity.id")
  List<Object[]> getAverageRatingsByActivity();

  // Query to get all feedback descriptions for processing keywords in service
  // layer
  @Query("SELECT f.feedbackDescription FROM EFeedback f WHERE f.feedbackDescription IS NOT NULL")
  List<String> getAllFeedbackDescriptions();

  @Query("SELECT AVG(f.rating) FROM EFeedback f WHERE f.activity.id = :activityId")
  Double getAverageRatingForActivity(@Param("activityId") Long activityId);

  /**
   * Count feedbacks for a specific activity
   * 
   * @param activityId ID of the activity
   * @return Count of feedbacks for the activity
   */
  @Query("SELECT COUNT(f) FROM EFeedback f WHERE f.activity.id = :activityId")
  Long countByActivityId(@Param("activityId") Long activityId);

  // Organization-specific queries

  // Get average rating for all activities of an organization
  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.organization.id = :organizationId")
  Double getAverageRatingForOrganization(@Param("organizationId") Long organizationId);

  // Count total feedbacks for an organization
  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.organization.id = :organizationId")
  Long countTotalFeedbacksForOrganization(@Param("organizationId") Long organizationId);

  // Get feedback descriptions for an organization (for keyword analysis)
  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.organization.id = :organizationId AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsForOrganization(@Param("organizationId") Long organizationId);

  // Get best rated activities for an organization
  @Query("SELECT a, AVG(f.rating) as avgRating FROM EFeedback f JOIN f.activity a " +
      "WHERE a.organization.id = :organizationId " +
      "GROUP BY a " +
      "HAVING COUNT(f) >= :minFeedbacks " +
      "ORDER BY avgRating DESC")
  List<Object[]> getBestRatedActivitiesForOrganization(
      @Param("organizationId") Long organizationId,
      @Param("minFeedbacks") Long minFeedbacks,
      Pageable pageable);

  // Get average rating by category for an organization
  @Query("SELECT a.activityCategory, AVG(f.rating) FROM EFeedback f JOIN f.activity a " +
      "WHERE a.organization.id = :organizationId " +
      "GROUP BY a.activityCategory")
  List<Object[]> getAverageRatingByCategoryForOrganization(@Param("organizationId") Long organizationId);

  // Get average rating trend by month for an organization
  @Query("SELECT FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate), AVG(f.rating) " +
      "FROM EFeedback f JOIN f.activity a " +
      "WHERE a.organization.id = :organizationId " +
      "AND a.startDate BETWEEN :startDate AND :endDate " +
      "GROUP BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate) " +
      "ORDER BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate)")
  List<Object[]> getRatingTrendByMonthForOrganization(
      @Param("organizationId") Long organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // New filtered queries
  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate")
  Double getAverageRatingInTimeRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category")
  Double getAverageRatingByCategory(@Param("category") ActivityCategory category);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.status = :status")
  Double getAverageRatingByStatus(@Param("status") ActivityStatus status);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category")
  Double getAverageRatingByTimeAndCategory(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("category") ActivityCategory category);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.status = :status")
  Double getAverageRatingByTimeAndStatus(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("status") ActivityStatus status);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category AND a.status = :status")
  Double getAverageRatingByCategoryAndStatus(@Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND a.status = :status")
  Double getAverageRatingByTimeAndCategoryAndStatus(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate")
  Long countReviewsInTimeRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category")
  Long countReviewsByCategory(@Param("category") ActivityCategory category);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.status = :status")
  Long countReviewsByStatus(@Param("status") ActivityStatus status);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category")
  Long countReviewsByTimeAndCategory(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("category") ActivityCategory category);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.status = :status")
  Long countReviewsByTimeAndStatus(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("status") ActivityStatus status);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category AND a.status = :status")
  Long countReviewsByCategoryAndStatus(@Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT COUNT(f) FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND a.status = :status")
  Long countReviewsByTimeAndCategoryAndStatus(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("category") ActivityCategory category, @Param("status") ActivityStatus status);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsInTimeRange(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByCategory(@Param("category") ActivityCategory category);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.status = :status AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByStatus(@Param("status") ActivityStatus status);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByTimeAndCategory(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("category") ActivityCategory category);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.status = :status AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByTimeAndStatus(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("status") ActivityStatus status);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.activityCategory = :category AND a.status = :status AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByCategoryAndStatus(@Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT f.feedbackDescription FROM EFeedback f JOIN f.activity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND a.status = :status AND f.feedbackDescription IS NOT NULL")
  List<String> getFeedbackDescriptionsByTimeAndCategoryAndStatus(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);
}
