package com.jfi.api.employee.usecase;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.in.EmployeeService;
import com.jfi.api.employee.port.out.EmployeePersistence;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeePersistence employeePersistence;

    public EmployeeServiceImpl(EmployeePersistence employeePersistence) {
        this.employeePersistence = employeePersistence;
    }

    @Override
    public Flux<Employee> findAllEmployees() {
        return employeePersistence.getEmployees();
    }

    @Override
    public Mono<Employee> findEmployeeById(UUID uuid) {
        return employeePersistence.getEmployeeById(uuid);
    }
}
