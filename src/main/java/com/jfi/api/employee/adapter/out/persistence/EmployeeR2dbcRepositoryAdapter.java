package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.out.EmployeeRepository;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Primary
@Component
public class EmployeeR2dbcRepositoryAdapter implements EmployeeRepository {

    private final EmployeeR2dbcRepository r2dbcRepository;

    public EmployeeR2dbcRepositoryAdapter(
        EmployeeR2dbcRepository r2dbcRepository
    ) {
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
}
