package com.astraval.coreflow.employee_module.employee.dto;

import com.astraval.coreflow.employee_module.leavelog.dto.LeaveLogDto;
import com.astraval.coreflow.employee_module.worklog.dto.WorkLogDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeActivityLogsDto {
    private List<WorkLogDto> workLogs;
    private List<LeaveLogDto> leaveLogs;
}
