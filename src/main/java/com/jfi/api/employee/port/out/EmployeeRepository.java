package com.jfi.api.employee.port.out;

import com.jfi.api.employee.domain.Employee;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {
    Flux<Employee> getEmployees();

    Mono<Employee> getEmployeeById(UUID uuid);
}
