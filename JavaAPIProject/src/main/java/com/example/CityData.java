package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class CityData {

    public static String getRawCityData(String city) throws Exception{
        String formattedCity = "";
        ArrayList<String> individualStrings = new ArrayList<String>();
        int current = 0;
        for (int i = 0; i < city.length(); i++) {
            if (city.charAt(i) == ' ') {
                String newString = city.substring(current, i);
                individualStrings.add(newString);
                current = i + 1;
            }
        }
        individualStrings.add(city.substring(current));
        for (int i = 0; i < individualStrings.size(); i++) {
            String word = individualStrings.get(i);
            String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            formattedCity += capitalized;
            if (i != individualStrings.size() - 1) {
                formattedCity += "%20";
            }
        }
        return API.getData("https://api.api-ninjas.com/v1/city?name=" + formattedCity + "&X-Api-Key=QHEz3f/fUpiN3YccGV99Pg==ooUgg8RLuVBv212Y");
    }
    public static int getPopulation(String city) throws Exception {
        String raw = getRawCityData(city);
        JSONArray jsonArray = new JSONArray(raw);
        JSONObject cityData = jsonArray.getJSONObject(0);
        return cityData.getInt("population");
    }
}