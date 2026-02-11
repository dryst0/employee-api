package com.jfi.api.employee.usecase;

import com.jfi.api.employee.domain.Employee;
import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.domain.InvalidEmployeeException;
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

    @Override
    public Mono<Employee> createEmployee(Employee employee) {
        return Mono.defer(() -> {
            validateEmployee(employee);
            return employeePersistence.saveEmployee(employee);
        });
    }

    @Override
    public Mono<Employee> updateEmployee(UUID uuid, Employee employee) {
        return employeePersistence
            .getEmployeeById(uuid)
            .switchIfEmpty(Mono.error(new EmployeeNotFoundException(uuid)))
            .flatMap(existing ->
                Mono.defer(() -> {
                    validateEmployee(employee);
                    employee.setUuid(uuid);
                    return employeePersistence.saveEmployee(employee);
                })
            );
    }

    private void validateEmployee(Employee employee) {
        if (
            employee.getFirstName() == null || employee.getFirstName().isBlank()
        ) {
            throw new InvalidEmployeeException("First name must not be blank");
        }
        if (
            employee.getLastName() == null || employee.getLastName().isBlank()
        ) {
            throw new InvalidEmployeeException("Last name must not be blank");
        }
        if (employee.getEmployeeType() == null) {
            throw new InvalidEmployeeException(
                "Employee type must not be null"
            );
        }
    }
}
