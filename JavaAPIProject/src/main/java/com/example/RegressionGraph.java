package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class RegressionGraph {
    private SimpleRegression regression;
    private ArrayList<String> validCities;    
    private ArrayList<Integer> populations;
    private ArrayList<Integer> AQIs;
    private double rSquared;

    public RegressionGraph(int sampleNumbers) throws Exception {
        regression = new SimpleRegression();
        this.validCities = new ArrayList<>();
        this.populations = new ArrayList<>();
        this.AQIs = new ArrayList<>();
        sampleValidCities(sampleNumbers);
        populateRegressionModel();
    }

    public ArrayList<Integer> getPopulations() {
        return populations;
    }

    public ArrayList<Integer> getAQIs(){
        return AQIs;
    }

    public double getRSquared() {
        return rSquared;
    }

    public ArrayList<String> sampleValidCities(int num) throws Exception {
        ArrayList<String> allCities = new ArrayList<>();
        String filePath = "/workspaces/final-project-antisepticsprays/JavaAPIProject/src/main/java/com/example/AllCities.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String city;
            while ((city = br.readLine()) != null) {
                if (!city.trim().isEmpty()) {
                    allCities.add(city.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        java.util.Random rand = new java.util.Random();
        while (validCities.size() < num && validCities.size() < allCities.size()) {
            int index = rand.nextInt(allCities.size());
            String candidateCity = allCities.get(index);

            if (validCities.contains(candidateCity)) continue;

            try {
                int population = CityData.getPopulation(candidateCity);
                int aqi = AirQualityData.getOverallAQI(candidateCity);

                validCities.add(candidateCity);
                populations.add(population);
                AQIs.add(aqi);
            } catch (Exception e) {
                System.out.println("Skipping city due to API error: " + candidateCity);
            }
        }
        return validCities;
    }

    public void populateRegressionModel() {
        for (int i = 0; i < populations.size(); i++) {
            regression.addData(populations.get(i), AQIs.get(i));
        }
        rSquared = regression.getRSquare();
    }

    public String getRegressionEquation() {
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        return String.format("AQI = %.8f * population + %.8f", slope, intercept);
    }

    public void printSummary() {
        System.out.println("=== Regression Summary ===");
        System.out.println("Number of samples: " + validCities.size());
        System.out.println("Cities used: " + String.join(", ", validCities));
        System.out.println("Regression Equation: " + getRegressionEquation());
        System.out.printf("R² (Coefficient of Determination): %.4f%n", rSquared);
    }
}