package com.winnguyen1905.Activity.config.mapper;


import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityViewModel;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
  EActivity toEActivity(ActivityDto request);
  ActivityViewModel toActivityViewModel (EActivity eActivity);
  void updateActivity (@MappingTarget EActivity eActivity, ActivityDto activityDto);
}
