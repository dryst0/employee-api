package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.out.EmployeeRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FakeEmployeeRepository implements EmployeeRepository {

    private final Map<UUID, Employee> employees = new LinkedHashMap<>();

    public void save(Employee employee) {
        employees.put(employee.getUuid(), employee);
    }

    @Override
    public Flux<Employee> getEmployees() {
        return Flux.fromIterable(employees.values());
    }

    @Override
    public Mono<Employee> getEmployeeById(UUID uuid) {
        return Mono.justOrEmpty(employees.get(uuid));
    }
}
