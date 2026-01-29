package com.airline.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class FlightData {
    private int id;
    private String flightNumber;
    private String departureTime;
    private String arrivalTime;
    private double economyFare;
    private double businessFare;
    private double firstClassFare;
    private LocalDate departureDate;
    private String departureCity;
    private String destinationCity;

    public FlightData(int id, String flightNumber, Timestamp departureTime, Timestamp arrivalTime, double economyFare, double businessFare, double firstClassFare, String departureCity, String destinationCity) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.departureTime = departureTime.toString();
        this.arrivalTime = arrivalTime.toString();
        this.economyFare = economyFare;
        this.businessFare = businessFare;
        this.firstClassFare = firstClassFare;
        this.departureDate = departureTime.toLocalDateTime().toLocalDate();
        this.departureCity = departureCity;
        this.destinationCity = destinationCity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public double getEconomyFare() {
        return economyFare;
    }

    public double getBusinessFare() {
        return businessFare;
    }

    public double getFirstClassFare() {
        return firstClassFare;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }
}
