package com.eric.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
    }

    public static ObjectMapper getMapper(){
        return mapper;
    }

    public String parseToString(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }


}
