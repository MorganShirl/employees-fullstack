package com.morgan.backend.services;

import com.morgan.backend.entities.Employee;
import com.morgan.backend.exceptions.NotFoundException.EmployeeNotFoundException;
import com.morgan.backend.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    // Cache for read operations
    @Cacheable("employeesAll")
    public List<Employee> findAll() {
        log.info("DB hit for findAll");
        return employeeRepository.findAll();
    }

    @Cacheable(cacheNames = "employeesById", key = "#id")
    public Employee findById(Long id) {
        log.info("DB hit for findById {}", id);
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    // Evict for write operations
    @CacheEvict(cacheNames = {"employeesAll", "employeesById"}, allEntries = true)
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    @CacheEvict(cacheNames = {"employeesAll", "employeesById"}, allEntries = true)
    public Employee upsert(Long id, Employee employeeEntity) {
        return employeeRepository.findById(id)
            .map(existing -> {
                existing.setFirstName(employeeEntity.getFirstName());
                existing.setLastName(employeeEntity.getLastName());
                existing.setRole(employeeEntity.getRole());
                return employeeRepository.save(existing);
            })
            .orElseGet(() -> {
                employeeEntity.setId(id);
                return employeeRepository.save(employeeEntity);
            });
    }

    @CacheEvict(cacheNames = {"employeesAll", "employeesById"}, allEntries = true)
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }
}
