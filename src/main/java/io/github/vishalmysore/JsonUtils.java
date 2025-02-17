package io.github.vishalmysore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class JsonUtils {

    public static String fetchJson(String jsonQustions){
        if (jsonQustions.contains("```json") && jsonQustions.contains("```")) {
            int startIndex = jsonQustions.indexOf("```json") + 7; // Move past ```json
            int endIndex = jsonQustions.indexOf("```", startIndex); // Find closing ```

            if (endIndex != -1) {
                jsonQustions = jsonQustions.substring(startIndex, endIndex).trim();

            }
        }
        return jsonQustions;
    }




        public static String generateUniqueID() {
            UUID uuid = UUID.randomUUID();
            return uuid.toString(); // It returns a string representation of the UUID
        }
    public static String generateUniqueIDForUser(String userId) {
        return userId+"_"+generateUniqueID();
    }

    public static String toJson(Object obj) {

        ObjectMapper objectMapper = new ObjectMapper();

        // Convert Java object to JSON (Pretty print for readability)
        String json = null;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage());
        }
        return json;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        T obj = null;
        try {
            obj = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to object: {}", e.getMessage());
        }
        return obj;
    }
}
