package com.airline.ui;

import com.airline.database.DBConnection;
import com.airline.DataStructure.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Admin {

    private static final Queue<String> waitlist = new LinkedList<>();

    public static void handleAdminMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Admin Dashboard ---");
            System.out.println("1. Flight Management");
            System.out.println("2. Booking and Ticketing");
            System.out.println("3. Customer Management");
            System.out.println("4. Payment Management");
            System.out.println("5. Reports and Analysis");
            System.out.println("6. Manage Waitlist");
            System.out.println("7. Exit to Main Menu");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: handleFlightManagement(scanner); break;
                case 2: handleBookingManagement(scanner); break;
                case 3: handleCustomerManagement(scanner); break;
                case 4: handlePaymentManagement(); break;
                case 5: handleReports(); break;
                case 6: handleWaitlist(scanner); break;
                case 7: return;
                default: System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }

    private static void handleFlightManagement(Scanner scanner) {
        System.out.println("\n--- Flight Management ---");
        System.out.println("1. Add Flight");
        System.out.println("2. Edit Flight");
        System.out.println("3. Cancel Flight");
        System.out.println("4. View All Flights");
        System.out.println("5. Reschedule a Cancelled Flight");
        System.out.println("6. Back to Admin Dashboard");
        System.out.print("Enter choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        switch (choice) {
            case 1: addFlight(scanner); break;
            case 2: editFlight(scanner); break;
            case 3: cancelFlight(scanner); break;
            case 4: viewAllFlights(); break;
            case 5: rescheduleFlight(scanner); break;
            case 6: return;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void addFlight(Scanner scanner) {
        System.out.println("\n--- Add a New Flight ---");
        System.out.print("Enter Flight Number: ");
        String flightNumber = scanner.nextLine();

        displayAllAircraft();

        int aircraftId = 0;
        int aircraftCapacity = 0;
        while (true) {
            System.out.print("Enter a valid Aircraft ID to add flight: ");
            try {
                aircraftId = Integer.parseInt(scanner.nextLine());
                String sql = "SELECT total_seats FROM aircrafts WHERE aircraft_id = ?";
                Connection conn = DBConnection.getConnection();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, aircraftId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        aircraftCapacity = rs.getInt("total_seats");
                        System.out.println("✅ Aircraft selected. Capacity: " + aircraftCapacity + " seats.");
                        break;
                    } else {
                        System.out.println("❌ Error: Aircraft ID not found. Please try again.");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number for the Aircraft ID.");
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                return;
            }
        }

        int availableSeats;
        while (true) {
            System.out.print("Enter Total Available Seats: ");
            try {
                availableSeats = Integer.parseInt(scanner.nextLine());
                if (availableSeats > 0 && availableSeats <= aircraftCapacity) break;
                else System.out.println("❌ Error: Please enter a number between 1 and " + aircraftCapacity + ".");
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }

        System.out.print("Enter Departure City: ");
        String depCity = scanner.nextLine();
        System.out.print("Enter Destination City: ");
        String destCity = scanner.nextLine();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime departureTime;
        while (true) {
            System.out.print("Enter Departure Time (YYYY-MM-DD HH:MM:SS): ");
            try {
                departureTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                if (departureTime.isAfter(LocalDateTime.now())) break;
                else System.out.println("❌ Error: Departure date must be in the future.");
            } catch (DateTimeParseException e) {
                System.out.println("❌ Error: Invalid date format.");
            }
        }

        LocalDateTime arrivalTime;
        while (true) {
            System.out.print("Enter Arrival Time (YYYY-MM-DD HH:MM:SS): ");
            try {
                arrivalTime = LocalDateTime.parse(scanner.nextLine(), formatter);
                if (arrivalTime.isAfter(departureTime)) break;
                else System.out.println("❌ Error: Arrival time must be after departure time.");
            } catch (DateTimeParseException e) {
                System.out.println("❌ Error: Invalid date format.");
            }
        }

        System.out.print("Enter Economy Fare: ");
        double ecoFare = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter Business Fare: ");
        double busFare = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter First Class Fare: ");
        double firstFare = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter Flight Type (Domestic/International): ");
        String type = scanner.nextLine();

        String insertSql = "INSERT INTO flights (flight_number, aircraft_id, departure_city, destination_city, departure_time, arrival_time, economy_fare, business_fare, first_class_fare, available_seats, flight_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try( PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, flightNumber);
            stmt.setInt(2, aircraftId);
            stmt.setString(3, depCity);
            stmt.setString(4, destCity);
            stmt.setTimestamp(5, Timestamp.valueOf(departureTime));
            stmt.setTimestamp(6, Timestamp.valueOf(arrivalTime));
            stmt.setDouble(7, ecoFare);
            stmt.setDouble(8, busFare);
            stmt.setDouble(9, firstFare);
            stmt.setInt(10, availableSeats);
            stmt.setString(11, type);
            if (stmt.executeUpdate() > 0) System.out.println("\n✅ Flight added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding flight: " + e.getMessage());
        }
    }

    private static void editFlight(Scanner scanner) {
        Connection conn = DBConnection.getConnection();
        int flightIdToEdit;
        while (true) {
            System.out.print("\nEnter the Flight ID to edit (or '0' to go back): ");
            try {
                flightIdToEdit = Integer.parseInt(scanner.nextLine());
                if (flightIdToEdit == 0) return;
                String checkSql = "SELECT flight_number, departure_city, destination_city FROM flights WHERE flight_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                    stmt.setInt(1, flightIdToEdit);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.printf("\n✅ Flight Found: %s, From: %s, To: %s\n", rs.getString(1), rs.getString(2), rs.getString(3));
                        break;
                    } else System.out.println("❌ Error: Flight ID not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                return;
            }
        }

        while (true) {
            System.out.println("\nWhat would you like to edit?");
            System.out.println("1. Flight Number\n2. Departure and Arrival Times\n3. Fares\n4. Back to Menu");
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());
            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter new Flight Number: ");
                        updateFlightField(conn, flightIdToEdit, "flight_number", scanner.nextLine());
                        break;
                    case 2:
                        System.out.print("Enter new Departure Time (YYYY-MM-DD HH:MM:SS): ");
                        String newDepTime = scanner.nextLine();
                        System.out.print("Enter new Arrival Time (YYYY-MM-DD HH:MM:SS): ");
                        String newArrTime = scanner.nextLine();
                        try (PreparedStatement stmt = conn.prepareStatement("UPDATE flights SET departure_time = ?, arrival_time = ? WHERE flight_id = ?")) {
                            stmt.setTimestamp(1, Timestamp.valueOf(newDepTime));
                            stmt.setTimestamp(2, Timestamp.valueOf(newArrTime));
                            stmt.setInt(3, flightIdToEdit);
                            stmt.executeUpdate();
                            System.out.println("✅ Flight times updated.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter new Economy Fare: ");
                        double newEcoFare = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter new Business Fare: ");
                        double newBusFare = Double.parseDouble(scanner.nextLine());
                        System.out.print("Enter new First Class Fare: ");
                        double newFirstFare = Double.parseDouble(scanner.nextLine());
                        try (PreparedStatement stmt = conn.prepareStatement("UPDATE flights SET economy_fare = ?, business_fare = ?, first_class_fare = ? WHERE flight_id = ?")) {
                            stmt.setDouble(1, newEcoFare);
                            stmt.setDouble(2, newBusFare);
                            stmt.setDouble(3, newFirstFare);
                            stmt.setInt(4, flightIdToEdit);
                            stmt.executeUpdate();
                            System.out.println("✅ Fares updated.");
                        }
                        break;
                    case 4: return;
                    default: System.out.println("❌ Invalid choice.");
                }
            } catch (SQLException e) {
                System.err.println("Error updating flight: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Invalid data format.");
            }
        }
    }

    private static void updateFlightField(Connection conn, int flightId, String field, Object value) throws SQLException {
        String sql = "UPDATE flights SET " + field + " = ? WHERE flight_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, value);
            stmt.setInt(2, flightId);
            stmt.executeUpdate();
            System.out.println("✅ Field " + field + " updated successfully!");
        }
    }

    private static void cancelFlight(Scanner scanner) {
        System.out.print("Enter Flight ID to cancel: ");
        int flightId = Integer.parseInt(scanner.nextLine());
        String sql = "UPDATE flights SET status = 'Cancelled' WHERE flight_id = ?";
        Connection conn = DBConnection.getConnection();
        try ( PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, flightId);
            if (stmt.executeUpdate() > 0) {
                System.out.println("Flight cancelled. Related bookings must be handled.");
            } else {
                System.out.println("Flight ID not found.");
            }
        } catch(SQLException e) {
            System.err.println("Error cancelling flight: " + e.getMessage());
        }
    }

    private static void viewAllFlights() {
        CustomAircraftCache aircraftCache = new CustomAircraftCache();
        String sql = "SELECT * FROM flights";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- All Flights ---");
            System.out.printf("%-5s %-10s %-25s %-15s %-15s %-10s %-10s\n",
                    "ID", "FlightNo", "Aircraft Model", "From", "To", "Status", "Seats");
            while (rs.next()) {
                int aircraftId = rs.getInt("aircraft_id");
                String aircraftModel;
                if (aircraftCache.containsKey(aircraftId)) {
                    aircraftModel = aircraftCache.get(aircraftId);
                } else {
                    String aircraftSql = "SELECT model_name FROM aircrafts WHERE aircraft_id = ?";
                    try (PreparedStatement aircraftStmt = conn.prepareStatement(aircraftSql)) {
                        aircraftStmt.setInt(1, aircraftId);
                        ResultSet aircraftRs = aircraftStmt.executeQuery();
                        if (aircraftRs.next()) {
                            aircraftModel = aircraftRs.getString("model_name");
                            aircraftCache.put(aircraftId, aircraftModel);
                        } else {
                            aircraftModel = "Unknown";
                        }
                    }
                }
                System.out.printf("%-5d %-10s %-25s %-15s %-15s %-10s %-10d\n",
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        aircraftModel,
                        rs.getString("departure_city"),
                        rs.getString("destination_city"),
                        rs.getString("status"),
                        rs.getInt("available_seats"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching flights: " + e.getMessage());
        }
    }

    private static void handleBookingManagement(Scanner scanner) {
        System.out.println("\n--- Booking and Ticketing ---");
        System.out.println("1. View All Reservations");
        System.out.println("2. Export Reservations to file (reservations.txt)");
        System.out.println("3. Cancel a Booking and Process Refund");
        System.out.println("4. Back to Admin Dashboard");
        System.out.print("Enter choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        switch (choice) {
            case 1: viewAllReservations(); break;
            case 2: exportReservationsToFile(); break;
            case 3: cancelBooking(scanner); break;
            case 4: return;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void viewAllReservations() {
        String sql = "SELECT b.booking_id, p.full_name, f.flight_number, b.seat_class, b.booking_status FROM bookings b JOIN passengers p ON b.passenger_id = p.passenger_id JOIN flights f ON b.flight_id = f.flight_id";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- All Bookings ---");
            System.out.printf("%-10s %-25s %-15s %-15s %-15s\n", "BookingID", "Passenger", "Flight No.", "Class", "Status");
            while(rs.next()) {
                System.out.printf("%-10d %-25s %-15s %-15s %-15s\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
            }
        } catch(SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
    }

    private static void exportReservationsToFile() {
        String sql = "SELECT b.booking_id, p.full_name, p.email, f.flight_number, f.departure_city, f.destination_city, b.booking_status FROM bookings b JOIN passengers p ON b.passenger_id = p.passenger_id JOIN flights f ON b.flight_id = f.flight_id";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql); PrintWriter writer = new PrintWriter(new FileWriter("reservations.txt"))) {
            writer.println("--- All Reservations Report ---");
            writer.printf("%-10s %-25s %-30s %-15s %-20s %-15s\n", "BookingID", "Passenger", "Email", "Flight", "Route", "Status");
            writer.println("=".repeat(115));
            while (rs.next()) {
                writer.printf("%-10d %-25s %-30s %-15s %-20s %-15s\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5) + "->" + rs.getString(6), rs.getString(7));
            }
            System.out.println("Successfully exported to reservations.txt");
        } catch (SQLException | IOException e) {
            System.err.println("Error exporting reservations: " + e.getMessage());
        }
    }

    private static void cancelBooking(Scanner scanner) {
        System.out.print("Enter Booking ID to cancel: ");
        int bookingId = Integer.parseInt(scanner.nextLine());
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            int flightId;
            int numberOfSeats = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT flight_id, number_of_seats FROM bookings WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    flightId = rs.getInt("flight_id");
                    numberOfSeats = rs.getInt("number_of_seats");
                }
                else throw new SQLException("Booking ID not found.");
            }
            // UPDATED: Set both booking and payment status in one query on the 'bookings' table.
            try (PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET booking_status = 'Cancelled', payment_status = 'Refunded' WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            // Add seats back to flight
            try (PreparedStatement ps = conn.prepareStatement("UPDATE flights SET available_seats = available_seats + ? WHERE flight_id = ?")) {
                ps.setInt(1, numberOfSeats);
                ps.setInt(2, flightId);
                ps.executeUpdate();
            }
            conn.commit();
            System.out.println("Booking " + bookingId + " cancelled and refund processed.");
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) { System.err.println("Error on rollback: " + ex.getMessage()); }
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { System.err.println("Error resetting auto-commit: " + e.getMessage()); }
        }
    }

    private static void handleCustomerManagement(Scanner scanner) {
        System.out.println("\n--- Customer Management ---");
        System.out.println("1. View All Passengers");
        System.out.println("2. Export Passenger Details to file (passengerDetails.txt)");
        System.out.println("3. Back to Admin Dashboard");
        System.out.print("Enter choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        switch (choice) {
            case 1:
                viewAllPassengers();
                break;
            case 2:
                exportPassengerDetails();
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void viewAllPassengers() {
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT passenger_id, full_name, phone_number, email FROM passengers")) {
            System.out.println("\n--- All Passengers ---");
            System.out.printf("%-5s %-25s %-15s %-30s\n", "ID", "Name", "Phone", "Email");
            while(rs.next()) {
                System.out.printf("%-5d %-25s %-15s %-30s\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        } catch(SQLException e) {
            System.err.println("Error fetching passengers: " + e.getMessage());
        }
    }

    private static void exportPassengerDetails() {
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM passengers"); PrintWriter writer = new PrintWriter(new FileWriter("passengerDetails.txt"))) {
            writer.println("--- All Passenger Details ---");
            writer.printf("%-5s %-25s %-10s %-15s %-15s %-30s\n", "ID", "Name", "Gender", "Phone", "Nationality", "Email");
            writer.println("=".repeat(110));
            while (rs.next()) {
                writer.printf("%-5d %-25s %-10s %-15s %-15s %-30s\n", rs.getInt("passenger_id"), rs.getString("full_name"), rs.getString("gender"), rs.getString("phone_number"), rs.getString("nationality"), rs.getString("email"));
            }
            System.out.println("Successfully exported passenger details to passengerDetails.txt");
        } catch (SQLException | IOException e) {
            System.err.println("Error exporting passenger details: " + e.getMessage());
        }
    }

    private static void handlePaymentManagement() {
        // UPDATED: Query now selects payment info from the 'bookings' table.
        String sql = "SELECT booking_id, amount, payment_date, payment_status FROM bookings";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- All Transactions ---");
            System.out.printf("%-10s %-10s %-15s %-25s %-15s\n", "PaymentID", "BookingID", "Amount", "Date", "Status");
            // The payment_id is now the same as booking_id for simplicity.
            while(rs.next()) {
                System.out.printf("%-10d %-10d %-15.2f %-25s %-15s\n", rs.getInt("booking_id"), rs.getInt("booking_id"), rs.getDouble("amount"), rs.getTimestamp("payment_date").toString(), rs.getString("payment_status"));
            }
        } catch(SQLException e) {
            System.err.println("Error fetching payments: " + e.getMessage());
        }
    }

    private static void handleReports() {
        System.out.println("\n--- Reports and Analysis ---");
        System.out.println("1. Daily Flight Occupancy Report\n2. Sales & Revenue Report\n3. Popular Routes Analysis");
        System.out.print("Enter choice: ");
        Scanner scanner = new Scanner(System.in);
        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1: generateOccupancyReport(); break;
            case 2: generateSalesReport(); break;
            case 3: generatePopularRoutesReport(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void generateOccupancyReport() {
        String sql = "SELECT f.flight_number, a.total_seats, (a.total_seats - f.available_seats) AS occupied_seats FROM flights f JOIN aircrafts a ON f.aircraft_id = a.aircraft_id WHERE DATE(f.departure_time) = CURDATE()";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql); PrintWriter writer = new PrintWriter(new FileWriter("daily_occupancy.txt"))) {
            writer.println("--- Daily Flight Occupancy Report for " + java.time.LocalDate.now() + " ---");
            writer.printf("%-15s %-15s %-15s %-15s\n", "Flight No.", "Total Seats", "Occupied", "Occupancy %");
            writer.println("=".repeat(65));
            while (rs.next()) {
                int total = rs.getInt("total_seats");
                int occupied = rs.getInt("occupied_seats");
                double occupancy = (total > 0) ? ((double) occupied / total) * 100 : 0;
                writer.printf("%-15s %-15d %-15d %-15.2f%%\n", rs.getString("flight_number"), total, occupied, occupancy);
            }
            System.out.println("Successfully generated daily_occupancy.txt");
        } catch (SQLException | IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private static void generateSalesReport() {
        Connection conn = DBConnection.getConnection();
        // UPDATED: Queries now target the 'bookings' table for sales data.
        try (Statement stmt = conn.createStatement(); PrintWriter writer = new PrintWriter(new FileWriter("sales_report.txt"))) {
            writer.println("--- Sales & Revenue Report ---");
            ResultSet rs = stmt.executeQuery("SELECT SUM(amount) AS total_revenue FROM bookings WHERE payment_status = 'Completed'");
            if (rs.next()) writer.printf("Total Confirmed Revenue: ₹%.2f\n", rs.getDouble("total_revenue"));
            rs = stmt.executeQuery("SELECT COUNT(*) AS cancelled_count FROM bookings WHERE booking_status = 'Cancelled'");
            if (rs.next()) writer.printf("Total Cancelled Bookings: %d\n", rs.getInt("cancelled_count"));
            System.out.println("Successfully generated sales_report.txt");
        } catch (SQLException | IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private static void generatePopularRoutesReport() {
        String sql = "SELECT departure_city, destination_city, COUNT(*) as booking_count FROM flights f JOIN bookings b ON f.flight_id = b.flight_id GROUP BY departure_city, destination_city ORDER BY booking_count DESC";
        Connection conn = DBConnection.getConnection();
        try ( Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql); PrintWriter writer = new PrintWriter(new FileWriter("popular_routes.txt"))) {
            writer.println("--- Popular Routes Analysis ---");
            writer.printf("%-25s %-15s\n", "Route", "Booking Count");
            writer.println("=".repeat(45));
            while (rs.next()) {
                writer.printf("%-25s %-15d\n", rs.getString(1) + " -> " + rs.getString(2), rs.getInt(3));
            }
            System.out.println("Successfully generated popular_routes.txt");
        } catch (SQLException | IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    private static void handleWaitlist(Scanner scanner) {
        System.out.println("\n--- Flight Waitlist Management ---");
        System.out.println("1. Add Passenger to Waitlist\n2. View Waitlist\n3. Process Next Passenger");
        System.out.print("Enter choice: ");
        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1:
                System.out.print("Enter passenger name to add to waitlist: ");
                waitlist.offer(scanner.nextLine());
                System.out.println("Passenger has been added to the waitlist.");
                break;
            case 2:
                if (waitlist.isEmpty()) System.out.println("The waitlist is empty.");
                else {
                    System.out.println("Current Waitlist:");
                    int pos = 1;
                    for (String p : waitlist) System.out.println(pos++ + ". " + p);
                }
                break;
            case 3:
                if (waitlist.isEmpty()) System.out.println("No passengers on the waitlist.");
                else System.out.println("Processing: " + waitlist.poll() + ". You can now book their ticket.");
                break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void displayAllAircraft() {
        System.out.println("\n--- Available Aircraft in Fleet ---");
        System.out.printf("%-10s %-15s %-25s\n", "ID", "Airline", "Model");
        System.out.println("--------------------------------------------------");

        String sql = "SELECT aircraft_id, airline_name, model_name FROM aircrafts";
        Connection conn = DBConnection.getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.printf("%-10d %-15s %-25s\n",
                        rs.getInt("aircraft_id"),
                        rs.getString("airline_name"),
                        rs.getString("model_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching aircraft list: " + e.getMessage());
        }
        System.out.println("--------------------------------------------------\n");
    }

    private static void rescheduleFlight(Scanner scanner) {
        System.out.println("\n--- Reschedule a Cancelled Flight ---");
        Connection conn = DBConnection.getConnection();

        System.out.println("Fetching cancelled flights...");
        String selectSql = "SELECT flight_id, flight_number, departure_city, destination_city FROM flights WHERE status = 'Cancelled'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No cancelled flights found in the system.");
                return;
            }

            System.out.println("\n--- Cancelled Flights ---");
            System.out.printf("%-5s %-15s %-20s %-20s\n", "ID", "Flight No.", "From", "To");
            System.out.println("----------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-15s %-20s %-20s\n",
                        rs.getInt("flight_id"),
                        rs.getString("flight_number"),
                        rs.getString("departure_city"),
                        rs.getString("destination_city"));
            }
            System.out.println("----------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Error fetching cancelled flights: " + e.getMessage());
            return;
        }

        try {
            System.out.print("\nEnter the ID of the flight you want to reschedule: ");
            int flightId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter new Departure Time (YYYY-MM-DD HH:MM:SS): ");
            String newDepTimeStr = scanner.nextLine();

            System.out.print("Enter new Arrival Time (YYYY-MM-DD HH:MM:SS): ");
            String newArrTimeStr = scanner.nextLine();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime newDepTime = LocalDateTime.parse(newDepTimeStr, formatter);
            LocalDateTime newArrTime = LocalDateTime.parse(newArrTimeStr, formatter);

            if (newDepTime.isBefore(LocalDateTime.now())) {
                System.out.println("❌ Error: New departure time must be in the future.");
                return;
            }
            if (newArrTime.isBefore(newDepTime)) {
                System.out.println("❌ Error: New arrival time must be after the new departure time.");
                return;
            }

            String updateSql = "UPDATE flights SET status = 'Scheduled', departure_time = ?, arrival_time = ? WHERE flight_id = ? AND status = 'Cancelled'";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setTimestamp(1, Timestamp.valueOf(newDepTime));
                updateStmt.setTimestamp(2, Timestamp.valueOf(newArrTime));
                updateStmt.setInt(3, flightId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("\n✅ Flight ID " + flightId + " has been successfully rescheduled!");
                } else {
                    System.out.println("❌ Error: Flight ID not found or it was not cancelled. No changes were made.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a valid number for the flight ID.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Please use YYYY-MM-DD HH:MM:SS.");
        } catch (SQLException e) {
            System.err.println("Database error during rescheduling: " + e.getMessage());
        }
    }
}

class WaitlistQueue {

    private static class Node {
        String passengerName;
        Node next;

        Node(String passengerName) {
            this.passengerName = passengerName;
            this.next = null;
        }
    }

    private Node head = null;
    private Node tail = null;

    public void add(String passengerName) {
        Node newNode = new Node(passengerName);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    public String remove() {
        if (isEmpty()) {
            return null;
        }
        String passengerName = head.passengerName;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        return passengerName;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public List<String> view() {
        List<String> passengers = new ArrayList<>();
        Node current = head;
        while (current != null) {
            passengers.add(current.passengerName);
            current = current.next;
        }
        return passengers;
    }
}
