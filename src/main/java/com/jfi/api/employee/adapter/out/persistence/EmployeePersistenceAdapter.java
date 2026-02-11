package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.out.EmployeePersistence;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EmployeePersistenceAdapter implements EmployeePersistence {

    private final EmployeeR2dbcRepository r2dbcRepository;

    public EmployeePersistenceAdapter(EmployeeR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Flux<Employee> getEmployees() {
        return r2dbcRepository.findAll();
    }

    @Override
    public Mono<Employee> getEmployeeById(UUID uuid) {
        return r2dbcRepository.findById(uuid);
    }

    @Override
    public Mono<Employee> saveEmployee(Employee employee) {
        return r2dbcRepository.save(employee);
    }
}
