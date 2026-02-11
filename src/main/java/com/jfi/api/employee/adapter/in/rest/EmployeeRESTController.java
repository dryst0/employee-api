package com.jfi.api.employee.adapter.in.rest;

import com.jfi.api.employee.domain.EmployeeNotFoundException;
import com.jfi.api.employee.port.in.EmployeeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/employees")
public class EmployeeRESTController {

    private final EmployeeService employeeService;

    public EmployeeRESTController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Flux<EmployeeDTO> getAllEmployees() {
        return employeeService.findAllEmployees().map(EmployeeDTO::from);
    }

    @GetMapping("/{uuid}")
    public Mono<EmployeeDTO> getEmployeeById(@PathVariable UUID uuid) {
        return employeeService
            .findEmployeeById(uuid)
            .map(EmployeeDTO::from)
            .switchIfEmpty(Mono.error(new EmployeeNotFoundException(uuid)));
    }

    @PostMapping
    public Mono<ResponseEntity<EmployeeDTO>> createEmployee(
        @Valid @RequestBody EmployeeRequest request
    ) {
        return employeeService
            .createEmployee(request.toEmployee())
            .map(EmployeeDTO::from)
            .map(dto ->
                ResponseEntity.created(
                    URI.create("/employees/" + dto.uuid())
                ).body(dto)
            );
    }
}
