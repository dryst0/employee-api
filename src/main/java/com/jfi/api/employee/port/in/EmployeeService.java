package com.jfi.api.employee.port.in;

import com.jfi.api.employee.domain.Employee;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeService {
    Flux<Employee> findAllEmployees();

    Mono<Employee> findEmployeeById(UUID uuid);

    Mono<Employee> createEmployee(Employee employee);

    Mono<Employee> updateEmployee(UUID uuid, Employee employee);
}
