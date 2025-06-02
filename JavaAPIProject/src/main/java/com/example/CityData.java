package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class CityData {

    // retrieves the population of a given city using the provided API key
    public static int getPopulation(String city, String key) throws Exception {
        String formattedCity = "";
        ArrayList<String> individualStrings = new ArrayList<String>();
        int current = 0;

        // splits the city name into individual words based on spaces
        for (int i = 0; i < city.length(); i++) {
            if (city.charAt(i) == ' ') {
                String newString = city.substring(current, i);
                individualStrings.add(newString);
                current = i + 1;
            }
        }

        // adds the final word after the last space
        individualStrings.add(city.substring(current));

        // capitalizes each word and joins them with '%20' to match URL encoding for the API
        for (int i = 0; i < individualStrings.size(); i++) {
            String word = individualStrings.get(i);
            String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            formattedCity += capitalized;
            if (i != individualStrings.size() - 1) {
                formattedCity += "%20";
            }
        }

        // constructs the API request URL using the formatted city name and the API key
        String raw = API.getData("https://api.api-ninjas.com/v1/city?name=" + formattedCity + "&X-Api-Key=" + key);

        // parses the JSON array response and retrieves the first city's population
        JSONArray jsonArray = new JSONArray(raw);
        JSONObject cityData = jsonArray.getJSONObject(0);
        return cityData.getInt("population");
    }
}