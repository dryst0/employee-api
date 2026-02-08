package com.jfi.api.employee.usecase;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.port.in.EmployeeService;
import com.jfi.api.employee.port.out.EmployeeRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Flux<Employee> findAllEmployees() {
        return employeeRepository.getEmployees();
    }

    @Override
    public Mono<Employee> findEmployeeById(UUID uuid) {
        return employeeRepository.getEmployeeById(uuid);
    }
}
