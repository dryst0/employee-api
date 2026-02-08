package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeType;
import com.jfi.api.employee.port.out.EmployeeRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final Map<UUID, Employee> employees = new HashMap<>();

    public EmployeeRepositoryImpl() {
        Employee worker = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Juan")
            .lastName("dela Cruz")
            .employeeType(EmployeeType.WORKER)
            .build();
        Employee manager = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Maria")
            .lastName("Santos")
            .employeeType(EmployeeType.MANAGER)
            .build();
        Employee financeManager = Employee.builder()
            .uuid(UUID.randomUUID())
            .firstName("Pedro")
            .lastName("Reyes")
            .employeeType(EmployeeType.FINANCE_MANAGER)
            .build();

        employees.put(worker.getUuid(), worker);
        employees.put(manager.getUuid(), manager);
        employees.put(financeManager.getUuid(), financeManager);
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
