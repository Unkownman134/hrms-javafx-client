package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class EmployeeManagerService {

    private static final String ENDPOINT = "/employees";


    public List<Employee> getAllEmployees(String authToken) throws IOException, InterruptedException {

        Optional<List<Employee>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<Employee>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}