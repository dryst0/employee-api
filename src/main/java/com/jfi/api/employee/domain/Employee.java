package com.jfi.api.employee.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(includeFieldNames = true)
public class Employee {

    private UUID uuid;
    private String firstName;
    private String lastName;

    @Builder.Default
    private EmployeeType employeeType = EmployeeType.WORKER;
}
