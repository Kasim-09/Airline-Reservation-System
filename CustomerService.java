package com.airline.Service;

import com.airline.DataStructure.CustomFlightList;
import com.airline.database.DBConnection;
import com.airline.model.FlightData;
import java.sql.*;
import java.time.LocalDate;

public class CustomerService {

    // REMOVED: The getUserId() method is no longer needed.

    /**
     * UPDATED: Now filters reservations directly by the logged-in passenger's ID.
     */
    public void viewMyReservations(int passengerId) {
        // The query now joins bookings directly with flights and filters by passenger_id.
        String sql = "SELECT b.booking_id, f.flight_number, f.departure_city, f.destination_city, f.departure_time, b.seat_class, b.seat_number, b.booking_status " +
                "FROM bookings b " +
                "JOIN flights f ON b.flight_id = f.flight_id " +
                "WHERE b.passenger_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, passengerId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("\nYou have no reservations.");
                return;
            }

            System.out.println("\n--- Your Reservations ---");
            System.out.printf("%-10s %-15s %-25s %-25s %-15s %-10s %-15s\n", "Booking ID", "Flight No.", "Route", "Departure Time", "Class", "Seat", "Status");
            System.out.println("=".repeat(125));
            while (rs.next()) {
                System.out.printf("%-10d %-15s %-25s %-25s %-15s %-10s %-15s\n",
                        rs.getInt("booking_id"),
                        rs.getString("flight_number"),
                        rs.getString("departure_city") + " -> " + rs.getString("destination_city"),
                        rs.getTimestamp("departure_time").toString(),
                        rs.getString("seat_class"),
                        rs.getString("seat_number"),
                        rs.getString("booking_status"));
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching your reservations: " + e.getMessage());
        }
    }

    /**
     * UPDATED: Now verifies ownership of the booking using passenger_id.
     */
    public boolean cancelMyReservation(int bookingId, int passengerId) {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // This query now directly checks if the booking belongs to the passenger.
            String checkSql = "SELECT flight_id, number_of_seats FROM bookings WHERE booking_id = ? AND passenger_id = ?";
            int flightId = -1;
            int numberOfSeats = 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookingId);
                checkStmt.setInt(2, passengerId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    flightId = rs.getInt("flight_id");
                    numberOfSeats = rs.getInt("number_of_seats");
                } else {
                    System.out.println("❌ Error: Booking ID not found or you do not have permission to cancel it.");
                    conn.rollback();
                    return false;
                }
            }

            // UPDATED: The payment status is now a column in the 'bookings' table.
            String updateBookingSql = "UPDATE bookings SET booking_status = 'Cancelled', payment_status = 'Refunded' WHERE booking_id = ?";
            try (PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingSql)) {
                updateBookingStmt.setInt(1, bookingId);
                updateBookingStmt.executeUpdate();
            }

            // Add the seats back to the flight's available seats
            try (PreparedStatement updateFlightStmt = conn.prepareStatement("UPDATE flights SET available_seats = available_seats + ? WHERE flight_id = ?")) {
                updateFlightStmt.setInt(1, numberOfSeats);
                updateFlightStmt.setInt(2, flightId);
                updateFlightStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Database error during cancellation: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) { System.err.println("Error during rollback: " + ex.getMessage()); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { System.err.println("Error resetting auto-commit: " + e.getMessage()); }
        }
    }

    // --- Flight search methods remain unchanged ---

    public CustomFlightList searchFlightsByDate(String departureCity, String destinationCity, String flightType, LocalDate travelDate) throws SQLException {
        CustomFlightList flights = new CustomFlightList();
        String sql = "SELECT * FROM flights WHERE departure_city = ? AND destination_city = ? AND flight_type = ? " +
                "AND DATE(departure_time) = ? AND available_seats > 0 AND status = 'Scheduled'";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, departureCity);
            stmt.setString(2, destinationCity);
            stmt.setString(3, flightType);
            stmt.setDate(4, java.sql.Date.valueOf(travelDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                flights.add(new FlightData(
                        rs.getInt("flight_id"), rs.getString("flight_number"),
                        rs.getTimestamp("departure_time"), rs.getTimestamp("arrival_time"),
                        rs.getDouble("economy_fare"), rs.getDouble("business_fare"),
                        rs.getDouble("first_class_fare"),rs.getString("departure_city"),
                        rs.getString("destination_city")
                ));
            }
        }
        return flights;
    }

    public CustomFlightList getAllFlightsByType(String flightType) throws SQLException {
        CustomFlightList flights = new CustomFlightList();
        String sql = "SELECT * FROM flights WHERE flight_type = ? AND available_seats > 0 AND status = 'Scheduled' AND departure_time > NOW()";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                flights.add(new FlightData(
                        rs.getInt("flight_id"), rs.getString("flight_number"),
                        rs.getTimestamp("departure_time"), rs.getTimestamp("arrival_time"),
                        rs.getDouble("economy_fare"), rs.getDouble("business_fare"),
                        rs.getDouble("first_class_fare"),
                        rs.getString("departure_city"), rs.getString("destination_city")
                ));
            }
        }
        return flights;
    }
}
