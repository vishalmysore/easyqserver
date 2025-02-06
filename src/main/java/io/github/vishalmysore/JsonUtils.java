package io.github.vishalmysore;

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
}
