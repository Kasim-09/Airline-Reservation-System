package com.airline.model;

import java.time.LocalDate;

public class PassengerInfo extends Person {
    private String dob;
    private String passengerType; // "Adult", "Child", or "Infant"

    public PassengerInfo(String type) {
        this.passengerType = type;
    }

    // Getters and Setters for PassengerInfo specific fields
    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    @Override
    public void displayDetails() {
        System.out.println("Passenger Name: " + getFullName());
        System.out.println("Date of Birth: " + dob);
        System.out.println("Type: " + passengerType);
    }
}
