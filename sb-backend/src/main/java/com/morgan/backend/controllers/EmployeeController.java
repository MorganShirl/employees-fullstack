package com.morgan.backend.controllers;

import com.morgan.backend.dtos.EmployeeDto;
import com.morgan.backend.entities.Employee;
import com.morgan.backend.mappers.EmployeeMapper;
import com.morgan.backend.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getEmployees() {
        log.info("Request getEmployees");
        List<Employee> employees = employeeService.findAll();
        return ResponseEntity.ok(employeeMapper.toDtoList(employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable Long id) {
        log.info("Request getEmployee [id={}]", id);
        Employee employee = employeeService.findById(id);
        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto newEmployeeDto) {
        log.info("Request createEmployee [newEmployeeDto={}]", newEmployeeDto);
        Employee toSave = employeeMapper.toEntity(newEmployeeDto);
        Employee saved  = employeeService.create(toSave); // evicts caches
        URI location = URI.create("/api/employees/" + saved.getId());
        return ResponseEntity.created(location).body(employeeMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> replaceEmployee(@Valid @RequestBody EmployeeDto newEmployeeDto, @PathVariable Long id) {
        log.info("Request replaceEmployee [newEmployeeDto={}], [id={}]", newEmployeeDto, id);
        Employee entity = employeeMapper.toEntity(newEmployeeDto);
        Employee saved = employeeService.upsert(id, entity); // evicts caches
        return ResponseEntity.ok(employeeMapper.toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("Request deleteEmployee [id={}]", id);
        employeeService.delete(id); // triggers @CacheEvict
        return ResponseEntity.noContent().build();
    }
}
