package com.jfi.api.employee.adapter.out.persistence;

import com.jfi.api.employee.domain.Employee;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EmployeeR2dbcRepository extends ReactiveCrudRepository<Employee, UUID> {}
