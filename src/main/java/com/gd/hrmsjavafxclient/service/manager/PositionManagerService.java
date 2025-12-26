package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class PositionManagerService {

    private static final String ENDPOINT = "/positions";


    public List<Position> getAllPositions(String authToken) throws IOException, InterruptedException {

        Optional<List<Position>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<Position>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}