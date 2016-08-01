package com.leadplatform.kfarmers.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonUtil
{
    public static String objectToJson(Object object) throws Exception
    {
        String data = null;
        ObjectMapper mapper = new ObjectMapper();

        data = mapper.writeValueAsString(object);

        return data;
    }

    public static Object jsonToObject(String data, Class<?> cls) throws Exception
    {
        Object response = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        response = mapper.readValue(data, cls);

        return response;
    }
    
    public static <T> Object jsonToArrayObject(JsonNode data, Class<?> cls) throws Exception
    {
    	ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<T> arrayList = mapper.convertValue(data, mapper.getTypeFactory().constructCollectionType(List.class, cls));
		return arrayList;
	}

    public static JsonNode parseTree(String json) throws Exception
    {
        ObjectMapper om = new ObjectMapper();
        return om.readTree(json);
    }
}
