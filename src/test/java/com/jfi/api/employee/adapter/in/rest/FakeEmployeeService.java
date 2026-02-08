package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.in.EmployeeService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FakeEmployeeService implements EmployeeService {

    private final Map<UUID, Employee> employees = new LinkedHashMap<>();

    public void save(Employee employee) {
        employees.put(employee.getUuid(), employee);
    }

    @Override
    public Flux<Employee> findAllEmployees() {
        return Flux.fromIterable(employees.values());
    }

    @Override
    public Mono<Employee> findEmployeeById(UUID uuid) {
        return Mono.justOrEmpty(employees.get(uuid));
    }
}
