package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.port.in.EmployeeService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeRESTController {

    private final EmployeeService employeeService;

    public EmployeeRESTController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Flux<EmployeeDTO> getAllEmployees() {
        log.info("GET /employees");
        return employeeService.findAllEmployees().map(EmployeeDTO::from);
    }

    @GetMapping("/{uuid}")
    public Mono<EmployeeDTO> getEmployeeById(@PathVariable UUID uuid) {
        log.info("GET /employees/{}", uuid);
        return employeeService
            .findEmployeeById(uuid)
            .map(EmployeeDTO::from)
            .switchIfEmpty(Mono.error(new EmployeeNotFoundException(uuid)));
    }
}
