package com.morgan.backend.mappers;

import com.morgan.backend.dtos.EmployeeDto;
import com.morgan.backend.entities.Employee;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);

    Employee toEntity(EmployeeDto dto);

    List<EmployeeDto> toDtoList(List<Employee> employees);

    List<Employee> toEntityList(List<EmployeeDto> dtos);
}
