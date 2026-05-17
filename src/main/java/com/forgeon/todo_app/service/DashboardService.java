package com.forgeon.todo_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.forgeon.todo_app.constant.Classification;
import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.constant.TodoStatus;
import com.forgeon.todo_app.dto.DashboardCountDto;
import com.forgeon.todo_app.dto.DashboardDto;
import com.forgeon.todo_app.mapper.DashboardMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final DashboardMapper dashboardMapper;

	public DashboardDto dashboard() {
		return new DashboardDto(
				dashboardMapper.summary(),
				convertStatusLabels(dashboardMapper.countByStatus()),
				convertPriorityLabels(dashboardMapper.countByPriority()),
				convertClassificationLabels(dashboardMapper.countByClassification()),
				dashboardMapper.countByAssignee());
	}

	private List<DashboardCountDto> convertStatusLabels(List<DashboardCountDto> counts) {
		return counts.stream()
				.map(count -> new DashboardCountDto(TodoStatus.valueOf(count.getLabel()).getLabel(), count.getCount()))
				.toList();
	}

	private List<DashboardCountDto> convertPriorityLabels(List<DashboardCountDto> counts) {
		return counts.stream()
				.map(count -> {
					Priority priority = Priority.getById(Integer.valueOf(count.getLabel()));
					return new DashboardCountDto(priority == null ? count.getLabel() : priority.getLabel(), count.getCount());
				})
				.toList();
	}

	private List<DashboardCountDto> convertClassificationLabels(List<DashboardCountDto> counts) {
		return counts.stream()
				.map(count -> new DashboardCountDto(Classification.valueOf(count.getLabel()).getLabel(), count.getCount()))
				.toList();
	}
}
