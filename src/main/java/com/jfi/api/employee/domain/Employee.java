package com.jfi.api.employee.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(includeFieldNames = true)
@Table("employee")
public class Employee {

    @Id
    private UUID uuid;

    private String firstName;
    private String lastName;

    @Builder.Default
    private EmployeeType employeeType = EmployeeType.WORKER;
}
