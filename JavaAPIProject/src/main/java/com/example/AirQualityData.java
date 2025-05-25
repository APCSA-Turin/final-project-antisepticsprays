package com.example;

import java.util.ArrayList;

import org.json.JSONObject;

public class AirQualityData {

    public static String getRawAirQualityData(String city) throws Exception{
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
        return API.getData("https://api.api-ninjas.com/v1/airquality?city=" + formattedCity + "&X-Api-Key=QHEz3f/fUpiN3YccGV99Pg==ooUgg8RLuVBv212Y");
    }

    public static int getOverallAQI(String city) throws Exception {
        String raw = getRawAirQualityData(city);
        JSONObject json = new JSONObject(raw);
        return json.getInt("overall_aqi");
    }
}