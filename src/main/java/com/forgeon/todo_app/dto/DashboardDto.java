package com.forgeon.todo_app.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardDto {
	private DashboardSummaryDto summary;
	private List<DashboardCountDto> statusCounts;
	private List<DashboardCountDto> priorityCounts;
	private List<DashboardCountDto> classificationCounts;
	private List<DashboardCountDto> assigneeCounts;
}
