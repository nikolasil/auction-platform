package com.bidpoint.backend.exception.role;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RoleAlreadyExistsException extends ResponseStatusException {
    public RoleAlreadyExistsException(String str) {
        super(HttpStatus.ALREADY_REPORTED, "Role with name=" + str + " already exists");
    }
}
