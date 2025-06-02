package com.example;

import java.util.ArrayList;
import org.json.JSONObject;

public class AirQualityData {

    // requests the overall AQI (Air Quality Index) for a given city using the provided API key
    public static int getAQI(String city, String key) throws Exception {
        String formattedCity = "";
        ArrayList<String> individualStrings = new ArrayList<>();
        int current = 0;

        // split the city name into individual words based on spaces
        for (int i = 0; i < city.length(); i++) {
            if (city.charAt(i) == ' ') {
                String newString = city.substring(current, i);
                individualStrings.add(newString);
                current = i + 1;
            }
        }

        // add the final word after the last space
        individualStrings.add(city.substring(current));

        // capitalize each word and join them with '%20' to match URL encoding for the API
        for (int i = 0; i < individualStrings.size(); i++) {
            String word = individualStrings.get(i);
            String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            formattedCity += capitalized;
            if (i != individualStrings.size() - 1) {
                formattedCity += "%20";
            }
        }

        // construct the API request URL using the formatted city name and the API key
        String raw = API.getData("https://api.api-ninjas.com/v1/airquality?city=" + formattedCity + "&X-Api-Key=" + key);

        // retrieve the overall AQI value from the JSON response
        JSONObject json = new JSONObject(raw);
        return json.getInt("overall_aqi");
    }
}