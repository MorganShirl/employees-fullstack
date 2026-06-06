package com.morgan.backend.exceptions;

import static java.lang.String.format;

public abstract class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static class EmployeeNotFoundException extends NotFoundException {
        public EmployeeNotFoundException(Long id) {
            super(format("Could not find employee with id [=%s]", id));
        }
    }

    public static class UserNotFoundException extends NotFoundException {
        public UserNotFoundException(String username) {
            super(format("Authenticated user not found in DB [=%s]", username));
        }
    }
}
