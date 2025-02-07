package io.github.vishalmysore;

import java.util.UUID;

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
}
