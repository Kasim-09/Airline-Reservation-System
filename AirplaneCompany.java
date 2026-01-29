package com.airline.ui;

import com.airline.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;



public class AirplaneCompany {

    public static void handleCompanyMenu(Scanner scanner) {
        System.out.println("\n--- Airplane Company Portal ---");
        System.out.print("Please enter your airline company name to proceed (e.g., IndiGo): ");
        String companyName = scanner.nextLine();

        if (!airlineExists(companyName)) {
            System.out.println("No airline found with the name '" + companyName + "'. Returning to main menu.");
            return;
        }
        System.out.println("Welcome, " + companyName + "!");

        while (true) {
            System.out.println("\n--- " + companyName + " Dashboard ---");
            System.out.println("1. Add New Aircraft to Your Fleet");
            System.out.println("2. View Your Scheduled Flights");
            System.out.println("3. Ban an Aircraft");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addAircraft(scanner, companyName);
                    break;
                case 2:
                    viewScheduledFlights(companyName);
                    break;
                case 3:
                    banAircraft(scanner, companyName);
                    break;
                case 4:
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * 1. Adds a new aircraft to the fleet for the specified airline.
     * The aircraft_id is handled by AUTO_INCREMENT in the database.
     */
    private static void addAircraft(Scanner scanner, String airlineName) {
        System.out.println("\n--- Add New Aircraft to " + airlineName + "'s Fleet ---");
        try {
            System.out.print("Enter Aircraft Model (e.g., Airbus A321): ");
            String modelName = scanner.nextLine();

            System.out.print("Enter Total Seat Capacity: ");
            int totalSeats = Integer.parseInt(scanner.nextLine());

            String sql = "INSERT INTO aircrafts (airline_name, model_name, total_seats) VALUES (?, ?, ?)";
            Connection conn = DBConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, airlineName);
                stmt.setString(2, modelName);
                stmt.setInt(3, totalSeats);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("\n✅ Aircraft '" + modelName + "' added successfully to your fleet!");
                } else {
                    System.out.println("❌ Failed to add the aircraft.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Seat capacity must be a number.");
        } catch (SQLException e) {
            System.err.println("Database error while adding aircraft: " + e.getMessage());
        }
    }

    /**
     * 2. Displays all flights operated by the specified airline company.
     */
    private static void viewScheduledFlights(String companyName) {
        String sql = "SELECT flight_number, departure_city, destination_city, departure_time, status " +
                "FROM flights f JOIN aircrafts a ON f.aircraft_id = a.aircraft_id " +
                "WHERE a.airline_name = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No scheduled flights found for " + companyName);
                return;
            }

            System.out.println("\n--- Scheduled Flights for " + companyName + " ---");
            System.out.printf("%-15s %-15s %-15s %-25s %-15s\n", "Flight No.", "From", "To", "Departure Time", "Status");
            System.out.println("=".repeat(85));
            int count = 0;
            while (rs.next()) {
                System.out.printf("%-15s %-15s %-15s %-25s %-15s\n",
                        rs.getString("flight_number"),
                        rs.getString("departure_city"),
                        rs.getString("destination_city"),
                        rs.getTimestamp("departure_time").toString(),
                        rs.getString("status"));
                count++;
            }
            System.out.println("\nTotal flights found: " + count);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * 3. Bans an aircraft by updating its status to 'Banned'.
     */
    private static void banAircraft(Scanner scanner, String airlineName) {
        System.out.println("\n--- Ban an Aircraft from com.airline.Service ---");

        // First, display all active aircraft for this airline
        String selectSql = "SELECT aircraft_id, model_name FROM aircrafts WHERE airline_name = ? AND status = 'Active'";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, airlineName);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No active aircraft found for your airline to ban.");
                return;
            }

            System.out.println("Active Aircraft for " + airlineName + ":");
            System.out.printf("%-10s %-25s\n", "ID", "Model");
            System.out.println("------------------------------------");
            while(rs.next()) {
                System.out.printf("%-10d %-25s\n", rs.getInt("aircraft_id"), rs.getString("model_name"));
            }
            System.out.println("------------------------------------");

            // Now, ask which one to ban
            System.out.print("Enter the ID of the aircraft you want to ban: ");
            int aircraftIdToBan = Integer.parseInt(scanner.nextLine());

            String updateSql = "UPDATE aircrafts SET status = 'Banned' WHERE aircraft_id = ? AND airline_name = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, aircraftIdToBan);
                updateStmt.setString(2, airlineName);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("\n✅ Aircraft ID " + aircraftIdToBan + " has been banned. It cannot be assigned to new flights.");
                } else {
                    System.out.println("❌ Error: Aircraft ID not found or does not belong to your airline.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a valid number for the ID.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Helper method to verify that the airline exists.
     */
    private static boolean airlineExists(String companyName) {
        String sql = "SELECT 1 FROM aircrafts WHERE airline_name = ? LIMIT 1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }
}