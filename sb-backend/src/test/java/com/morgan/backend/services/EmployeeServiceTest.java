package com.morgan.backend.services;

import com.morgan.backend.entities.Employee;
import com.morgan.backend.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class EmployeeServiceTest {

    @Autowired
    EmployeeService employeeService;

    @MockitoBean
    EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        clearInvocations(employeeRepository);
    }

    @Test
    void testFindById_isCached() {
        // Given
        var john = new Employee();
        john.setId(1L);
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setRole("Dev");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(john));

        // When
        employeeService.findById(1L); // first call → DB hit
        employeeService.findById(1L); // second call → should be cache hit

        // Then
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAll_isCached_andEvictedOnCreate() {
        // Given
        var john = new Employee();
        john.setId(1L);
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setRole("Dev");

        var list1 = List.of(john);

        var jane = new Employee();
        jane.setId(2L);
        jane.setFirstName("Jane");
        jane.setLastName("Doe");
        jane.setRole("Dev");

        var list2 = List.of(john, jane);

        // First DB call returns [John], second DB call (after eviction) returns [John, Jane]
        when(employeeRepository.findAll()).thenReturn(list1, list2);

        // When
        var employees = employeeService.findAll(); // 1st → DB hit, caches [John]
        assertEquals(list1, employees);
        employees = employeeService.findAll(); // 2nd → cache hit, no DB call
        assertEquals(list1, employees);

        verify(employeeRepository, times(1)).findAll();

        // create() should evict caches
        when(employeeRepository.save(any(Employee.class))).thenReturn(jane);
        employeeService.create(jane);

        // After eviction, findAll should hit DB again and return second value
        employees = employeeService.findAll();
        assertEquals(list2, employees);

        // Then
        verify(employeeRepository, times(2)).findAll();
    }
}
