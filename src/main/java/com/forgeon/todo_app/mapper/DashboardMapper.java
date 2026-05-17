package com.forgeon.todo_app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.forgeon.todo_app.dto.DashboardCountDto;
import com.forgeon.todo_app.dto.DashboardSummaryDto;

@Mapper
public interface DashboardMapper {
	DashboardSummaryDto summary();

	List<DashboardCountDto> countByStatus();

	List<DashboardCountDto> countByPriority();

	List<DashboardCountDto> countByClassification();

	List<DashboardCountDto> countByAssignee();
}
