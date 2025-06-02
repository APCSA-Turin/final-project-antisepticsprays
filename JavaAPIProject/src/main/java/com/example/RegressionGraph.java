package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class RegressionGraph {
    public static Object RegressionGraph;
    private SimpleRegression regression;
    private ArrayList<String> validCities;    
    private ArrayList<Integer> populations;
    private ArrayList<Integer> AQIs;
    private double rSquared;

    // initializes the regression model by sampling valid cities and utilizing a simple regression library
    public RegressionGraph(int sampleNumbers, String key) throws Exception {
        regression = new SimpleRegression();
        this.validCities = new ArrayList<>();
        this.populations = new ArrayList<>();
        this.AQIs = new ArrayList<>();
        sampleValidCities(sampleNumbers, key);
        populateRegressionModel();
        rSquared = regression.getRSquare();
    }

    // returns list of population values for sampled cities
    public ArrayList<Integer> getPopulations() {
        return populations;
    }

    // returns list of AQI values for sampled cities
    public ArrayList<Integer> getAQIs(){
        return AQIs;
    }

    // returns the coefficient of determination (R²) of the regression (uses library)
    public double getRSquared() {
        return rSquared;
    }

    // returns the slope of the regression line (uses library)
    public double getSlope(){
        return regression.getSlope();
    }

    // returns the y-intercept of the regression line (uses library)
    public double getIntercept(){
        return regression.getIntercept();
    }

    // identifies cities that are outliers based on population using IQR
    public ArrayList<String> getOutliers() {
        ArrayList<String> outliers = new ArrayList<>();

        if (AQIs.size() < 4) return outliers; // not enough data for IQR -> outputs an empty list

        // create a sorted copy of the population list
        ArrayList<Integer> sortedPopulations = new ArrayList<>(populations);
        java.util.Collections.sort(sortedPopulations);

        // compute first and third quartiles
        double q1 = getPercentile(sortedPopulations, 25);
        double q3 = getPercentile(sortedPopulations, 75);
        double iqr = q3 - q1;

        // compute average AQI for thresholding
        double sum = 0;
        for (int aqi : AQIs) {
            sum += aqi;
        }
        double avg = sum / populations.size();

        // threshold = average AQI + 1.5 * IQR
        double threshold = avg + 1.5 * iqr;

        // identify cities whose population exceeds threshold
        for (int i = 0; i < AQIs.size(); i++) {
            if (populations.get(i) > threshold) {
                outliers.add(validCities.get(i));
            }
        }

        return outliers;
    }

    // helper method to calculate a percentile from a sorted list
    private double getPercentile(ArrayList<Integer> sortedList, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, Math.min(index, sortedList.size() - 1)));
    }

    // samples a specified number of valid cities and retrieves their AQI and population
    public ArrayList<String> sampleValidCities(int num, String key) throws Exception {
        ArrayList<String> allCities = new ArrayList<>();

        // clear existing data before sampling new cities
        validCities.clear();
        populations.clear();
        AQIs.clear();

        // load list of cities from CSV file of 1000 largest US cities
        // source: https://gist.github.com/Miserlou/11500b2345d3fe850c92
        String filePath = "JavaAPIProject\\src\\main\\java\\com\\example\\AllCities.csv";

        // read the CSV file line by line -> https://www.youtube.com/watch?v=zKDmzKaAQro&ab_channel=BroCode
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {               // ignore empty lines
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {                // ensure the line has at least two columns (e.g., rank, city)
                        String city = parts[1].trim();      // filters only the city name from the .csv file
                        allCities.add(city);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // error read for debugging
        }

        // randomly select unique cities until num-sized list of valid cities is reached
        java.util.Random rand = new java.util.Random();
        while (validCities.size() < num && validCities.size() < allCities.size()) {
            int index = rand.nextInt(allCities.size());
            String candidateCity = allCities.get(index);
            if (!validCities.contains(candidateCity)) { //skips duplicate cities
                try {
                    int population = CityData.getPopulation(candidateCity, key);
                    int aqi = AirQualityData.getAQI(candidateCity, key);
                    validCities.add(candidateCity);
                    populations.add(population);
                    AQIs.add(aqi);
                } catch (Exception e) { //skips city if API fails to retrieve information
                    System.out.println("Skipping city due to API error: " + candidateCity);
                    e.printStackTrace();  // error read for debugging
                }
            }
        }
        return validCities;
    }

    // adds data points to the regression model using population and AQI values from their respective lists
    public void populateRegressionModel() {
        for (int i = 0; i < populations.size(); i++) {
            regression.addData(populations.get(i), AQIs.get(i));
        }
    }

    // returns the regression equation in slope-intercept form
    public String getRegressionEquation() {
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        return String.format("AQI = %.8f * population + %.8f", slope, intercept);
    }

    // returns a summary of the regression analysis including R-Squared and cities used
    public String getSummary() {
    String citiesFormatted = "";
    for (int i = 0; i < validCities.size(); i++) {
        citiesFormatted += validCities.get(i);
        // adds a comma if it's not the last city
        if (i != validCities.size() - 1) {
            citiesFormatted += ", ";
        }
        // adds a new line every 8 cities, but not after the last city
        if ((i + 1) % 8 == 0 && i != validCities.size() - 1) {
            citiesFormatted += "\n";
        }
    }

    return 
        "=== Regression Summary ===\n" +
        "Number of samples: " + validCities.size() + "\n" + 
        "Cities used:\n" + citiesFormatted + "\n" +
        "Regression Equation: " + getRegressionEquation() + "\n" +
        String.format("R² (Coefficient of Determination): %.4f", rSquared);
}

    // for debugging
    public void printSummary() {
        System.out.println("=== Regression Summary ===");
        System.out.println("Number of samples: " + validCities.size());
        System.out.println("Cities used: " + String.join(", ", validCities));
        System.out.println("Regression Equation: " + getRegressionEquation());
        System.out.printf("R² (Coefficient of Determination): %.4f%n", rSquared);
    }
}