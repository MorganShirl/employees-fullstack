package com.morgan.backend.services;

import com.morgan.backend.entities.Employee;
import com.morgan.backend.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    void testCreateEmployee() {
        // given
        Employee employee = new Employee();
        employee.setFirstName("Morgan");

        when(employeeRepository.save(employee)).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Employee result = employeeService.create(employee);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(employee.getFirstName());
        verify(employeeRepository, times(1)).save(employee);
    }
}
