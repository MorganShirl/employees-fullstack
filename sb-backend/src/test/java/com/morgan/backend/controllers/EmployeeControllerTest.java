package com.morgan.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morgan.backend.dtos.EmployeeDto;
import com.morgan.backend.entities.Employee;
import com.morgan.backend.exceptions.NotFoundException.EmployeeNotFoundException;
import com.morgan.backend.mappers.EmployeeMapper;
import com.morgan.backend.mappers.EmployeeMapperImpl;
import com.morgan.backend.services.EmployeeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(EmployeeMapperImpl.class)
@AutoConfigureMockMvc(addFilters = false)  // ⟵ disable Spring Security filters
@TestInstance(Lifecycle.PER_CLASS) // for non-static @BeforeAll method
class EmployeeControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    MockMvc mockMvc; // Simulates HTTP requests

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EmployeeMapper employeeMapper;

    @MockitoBean
    Supplier<String> uuidSupplier;

    @MockitoBean
    EmployeeService employeeService;

    @MockitoBean
    CacheManager cacheManager;

    Employee employee;
    EmployeeDto employeeDto;

    @BeforeAll
    void setUpOnce() {
        // Prepare some dummy data
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setRole("Developer");

        employeeDto = employeeMapper.toDto(employee);
    }

    @Test
    void testGetAllEmployees() throws Exception {
        // Given
        when(employeeService.findAll()).thenReturn(List.of(employee));

        // When/Then
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(employeeService, times(1)).findAll();
    }

    @Test
    void testGetEmployeeById() throws Exception {
        // Given
        when(employeeService.findById(1L)).thenReturn(employee);

        // When/Then
        mockMvc.perform(get("/api/employees/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService, times(1)).findById(1L);
    }

    @Test
    void testGetEmployeeById_NotFound() throws Exception {
        // Given
        when(employeeService.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        // When/Then
        mockMvc.perform(get("/api/employees/{id}", 99L))
            .andExpect(status().isNotFound()) // Validates GlobalExceptionHandler works!
            .andExpect(jsonPath("$.title").value("Resource Not Found")); // Validates ProblemDetail

        verify(employeeService, times(1)).findById(99L);
    }

    @Test
    void testCreateEmployee() throws Exception {
        // Given
        // We use 'thenAnswer' to simulate the DB assigning an ID to the entity
        // created by the mapper.
        when(employeeService.create(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });

        // When/Then
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDto)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/employees/1"))
            .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService, times(1)).create(any(Employee.class));
    }

    @Test
    void testCreateInvalidEmployee() throws Exception {
        // Given an invalid DTO with null and empty values
        EmployeeDto invalidDto = new EmployeeDto(null, "morgan", null, "");

        // When/Then
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Error"))
            .andExpect(jsonPath("$.fieldErrors.length()").value(2))
            .andExpect(jsonPath("$.fieldErrors.lastName").value("Last name is required"))
            .andExpect(jsonPath("$.fieldErrors.role").value("Role is required"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        // Given
        doNothing().when(employeeService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/employees/{id}", 1L))
            .andExpect(status().isNoContent());

        verify(employeeService, times(1)).delete(1L);
    }

    @Test
    void testDeleteNonExistentEmployee() throws Exception {
        // Given
        doThrow(new EmployeeNotFoundException(99L)).when(employeeService).delete(99L);

        // When/Then
        mockMvc.perform(delete("/api/employees/{id}", 99L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"));

        verify(employeeService, times(1)).delete(99L);
    }

    @Test
    void testUpdateExistingEmployee() throws Exception {
        // Given
        EmployeeDto updateDto = new EmployeeDto(1L, "Jane", "Doe", "Developer");

        Employee updated = new Employee();
        updated.setId(1L);
        updated.setFirstName("Jane");
        updated.setLastName("Doe");
        updated.setRole("Developer");

        when(employeeService.upsert(eq(1L), any(Employee.class))).thenReturn(updated);

        // When/Then
        mockMvc.perform(put("/api/employees/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.role").value("Developer"));

        verify(employeeService, times(1)).upsert(eq(1L), any(Employee.class));
    }
}
