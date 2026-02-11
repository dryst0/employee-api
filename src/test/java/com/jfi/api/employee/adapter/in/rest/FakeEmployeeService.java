package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeNotFoundException;
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

    @Override
    public Mono<Employee> updateEmployee(UUID uuid, Employee employee) {
        if (!employees.containsKey(uuid)) {
            return Mono.error(new EmployeeNotFoundException(uuid));
        }
        employee.setUuid(uuid);
        employees.put(uuid, employee);
        return Mono.just(employee);
    }

    @Override
    public Mono<Employee> patchEmployee(UUID uuid, Employee employee) {
        if (!employees.containsKey(uuid)) {
            return Mono.error(new EmployeeNotFoundException(uuid));
        }
        Employee existing = employees.get(uuid);
        if (employee.getFirstName() != null) {
            existing.setFirstName(employee.getFirstName());
        }
        if (employee.getLastName() != null) {
            existing.setLastName(employee.getLastName());
        }
        if (employee.getEmployeeType() != null) {
            existing.setEmployeeType(employee.getEmployeeType());
        }
        return Mono.just(existing);
    }

    @Override
    public Mono<Void> deleteEmployee(UUID uuid) {
        if (!employees.containsKey(uuid)) {
            return Mono.error(new EmployeeNotFoundException(uuid));
        }
        employees.remove(uuid);
        return Mono.empty();
    }

    @Override
    public Mono<Employee> createEmployee(Employee employee) {
        if (employee.getUuid() == null) {
            employee.setUuid(UUID.randomUUID());
        }
        employees.put(employee.getUuid(), employee);
        return Mono.just(employee);
    }
}
