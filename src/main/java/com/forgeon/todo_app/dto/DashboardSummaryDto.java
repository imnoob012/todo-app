package com.forgeon.todo_app.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
	private Integer totalCount;
	private Integer overdueCount;
	private Integer dueThisWeekCount;
	private Integer doneCount;
	private Integer inProgressCount;
	private BigDecimal progressRate;
}
