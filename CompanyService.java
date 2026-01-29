package com.airline.Service; // Or your package name

import com.airline.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CompanyService {

    public int loginCompany(String username, String password) {
        String sql = "SELECT company_id FROM airline_companies WHERE username = ? AND password = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("company_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean registerCompany(String name, String user, String pass) {
        String sql = "INSERT INTO airline_companies (company_name, username, password) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, user);
            stmt.setString(3, pass);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public String getCompanyName(int companyId) {
        String sql = "SELECT company_name FROM airline_companies WHERE company_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("company_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * FULLY IMPLEMENTED: Adds a new aircraft to the fleet for the specified company.
     */
    public void addAircraft(Scanner scanner, int companyId, String airlineName) {
        System.out.println("\n--- Add New Aircraft to " + airlineName + "'s Fleet ---");
        try {
            System.out.print("Enter Aircraft Model (e.g., Airbus A321): ");
            String modelName = scanner.nextLine();
            System.out.print("Enter Total Seat Capacity: ");
            int totalSeats = Integer.parseInt(scanner.nextLine());

            String sql = "INSERT INTO aircrafts (company_id, airline_name, model_name, total_seats) VALUES (?, ?, ?, ?)";
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, companyId);
                stmt.setString(2, airlineName);
                stmt.setString(3, modelName);
                stmt.setInt(4, totalSeats);
                if (stmt.executeUpdate() > 0) {
                    System.out.println("\n✅ Aircraft '" + modelName + "' added successfully!");
                } else {
                    System.out.println("❌ Failed to add aircraft.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Seat capacity must be a number.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * FULLY IMPLEMENTED: Displays all flights for the specified company.
     */
    public void viewScheduledFlights(int companyId) {
        System.out.println("\n--- Scheduled Flights ---");
        String sql = "SELECT flight_number, departure_city, destination_city, departure_time, status FROM flights WHERE aircraft_id IN (SELECT aircraft_id FROM aircrafts WHERE company_id = ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No scheduled flights found for your airline.");
                return;
            }
            System.out.printf("%-15s %-15s %-15s %-25s %-15s\n", "Flight No.", "From", "To", "Departure Time", "Status");
            System.out.println("=".repeat(85));
            while (rs.next()) {
                System.out.printf("%-15s %-15s %-15s %-25s %-15s\n",
                        rs.getString("flight_number"), rs.getString("departure_city"),
                        rs.getString("destination_city"), rs.getTimestamp("departure_time").toString(),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * FULLY IMPLEMENTED: Bans an aircraft by updating its status.
     */
    public void banAircraft(Scanner scanner, int companyId) {
        System.out.println("\n--- Ban an Aircraft ---");
        String selectSql = "SELECT aircraft_id, model_name FROM aircrafts WHERE company_id = ? AND status = 'Active'";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No active aircraft found for your airline to ban.");
                return;
            }
            System.out.println("Active Aircraft:");
            System.out.printf("%-10s %-25s\n", "ID", "Model");
            System.out.println("------------------------------------");
            while(rs.next()) {
                System.out.printf("%-10d %-25s\n", rs.getInt("aircraft_id"), rs.getString("model_name"));
            }
            System.out.println("------------------------------------");

            System.out.print("Enter the ID of the aircraft to ban: ");
            int aircraftIdToBan = Integer.parseInt(scanner.nextLine());

            String updateSql = "UPDATE aircrafts SET status = 'Banned' WHERE aircraft_id = ? AND company_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, aircraftIdToBan);
                updateStmt.setInt(2, companyId);
                if (updateStmt.executeUpdate() > 0) {
                    System.out.println("\n✅ Aircraft ID " + aircraftIdToBan + " has been banned.");
                } else {
                    System.out.println("❌ Error: Aircraft ID not found or does not belong to your airline.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a valid number.");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
    /**
     * Unbans an aircraft by updating its status back to 'Active'.
     */
    public void unbanAircraft(Scanner scanner, int companyId) {
        System.out.println("\n--- Unban an Aircraft ---");

        // First, display all banned aircraft for this airline
        String selectSql = "SELECT aircraft_id, model_name FROM aircrafts WHERE company_id = ? AND status = 'Banned'";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No banned aircraft found for your airline to unban.");
                return;
            }

            System.out.println("Banned Aircraft:");
            System.out.printf("%-10s %-25s\n", "ID", "Model");
            System.out.println("------------------------------------");
            while(rs.next()) {
                System.out.printf("%-10d %-25s\n", rs.getInt("aircraft_id"), rs.getString("model_name"));
            }
            System.out.println("------------------------------------");

            // Now, ask which one to unban
            System.out.print("Enter the ID of the aircraft you want to unban: ");
            int aircraftIdToUnban = Integer.parseInt(scanner.nextLine());

            String updateSql = "UPDATE aircrafts SET status = 'Active' WHERE aircraft_id = ? AND company_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, aircraftIdToUnban);
                updateStmt.setInt(2, companyId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("\n✅ Aircraft ID " + aircraftIdToUnban + " has been unbanned and is now active.");
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
}