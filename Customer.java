package com.airline.ui;
import com.airline.DataStructure.CustomFlightList;
import com.airline.Service.CustomerService;
import com.airline.database.DBConnection;
import com.airline.model.BookingData;
import com.airline.model.FlightData;
import com.airline.model.PassengerInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Customer {

    public static void handleCustomerMenu(Scanner scanner) {
        BookingData bookingData = new BookingData();
        System.out.println("\n--- Customer Booking Portal ---");
        System.out.println("Please follow the steps to book your flight.");

        if (!collectTravelDetails(scanner, bookingData)) {
            System.out.println("Booking process cancelled.");
            return;
        }
        if (!collectPersonalInformation(scanner, bookingData)) {
            System.out.println("Booking process cancelled.");
            return;
        }
        if (!collectContactDetails(scanner, bookingData)) {
            System.out.println("Booking process cancelled.");
            return;
        }
        if (!processPayment(scanner, bookingData)) {
            System.out.println("Booking process cancelled.");
            return;
        }
        confirmAndSaveBooking(bookingData);
    }

    private static boolean collectTravelDetails(Scanner scanner, BookingData data) {
        System.out.println("\n--- Step 1: Travel Details ---");

        int typeChoice;
        while (true) {
            System.out.print("Enter Flight Type (1. Domestic, 2. International): ");
            try {
                typeChoice = Integer.parseInt(scanner.nextLine());
                if (typeChoice == 1 || typeChoice == 2) break;
                else System.out.println("Invalid choice. Please enter 1 or 2.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        String flightType = (typeChoice == 1) ? "Domestic" : "International";
        data.setFlightType(flightType);

        System.out.print("Enter Departure City: ");
        String departureCity = scanner.nextLine();
        System.out.print("Enter Destination City: ");
        String destinationCity = scanner.nextLine();

        LocalDate travelDate;
        while (true) {
            System.out.print("Enter Date of Travel (YYYY-MM-DD): ");
            String travelDateStr = scanner.nextLine();
            try {
                travelDate = LocalDate.parse(travelDateStr);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        CustomerService customerService = new CustomerService();
        CustomFlightList availableFlights = new CustomFlightList();

        try {
            availableFlights = customerService.searchFlightsByDate(departureCity, destinationCity, flightType, travelDate);
        } catch (SQLException e) {
            System.err.println("Database error during initial search: " + e.getMessage());
            return false;
        }

        if (availableFlights.isEmpty()) {
            System.out.println("\nNo flights found for your specific request. Here is a list of all available " + flightType + " flights:");
            try {
                availableFlights = customerService.getAllFlightsByType(flightType);
            } catch (SQLException e) {
                System.err.println("Database error fetching all flights: " + e.getMessage());
                return false;
            }
            if (availableFlights.isEmpty()) {
                System.out.println("There are currently no " + flightType + " flights available for booking in the entire system.");
                return false;
            }
        }

        System.out.println("\n--- Available Flights ---");
        System.out.printf("%-5s %-10s %-12s %-12s %-22s %-22s %-12s %-12s %-12s\n", "ID", "Flight No", "From", "To", "Departure", "Arrival", "Economy", "Business", "First Class");
        for (int i = 0; i < availableFlights.size(); i++) {
            FlightData flight = availableFlights.get(i);
            System.out.printf("%-5d %-10s %-12s %-12s %-22s %-22s %-12.2f %-12.2f %-12.2f\n", flight.getId(), flight.getFlightNumber(), flight.getDepartureCity(), flight.getDestinationCity(), flight.getDepartureTime(), flight.getArrivalTime(), flight.getEconomyFare(), flight.getBusinessFare(), flight.getFirstClassFare());
        }

        FlightData selectedFlight = null;
        while (true) {
            System.out.print("\nEnter the ID of the flight you want to book: ");
            try {
                int chosenFlightId = Integer.parseInt(scanner.nextLine());
                boolean found = false;
                for (int i = 0; i < availableFlights.size(); i++) {
                    FlightData flight = availableFlights.get(i);
                    if (flight.getId() == chosenFlightId) {
                        selectedFlight = flight;
                        found = true;
                        break;
                    }
                }
                if (found) break;
                else System.out.println("Invalid Flight ID. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        data.setFlightId(selectedFlight.getId());
        data.setDepartureDate(selectedFlight.getDepartureDate());

        int availableSeats = 0;
        String seatsSql = "SELECT available_seats FROM flights WHERE flight_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(seatsSql)) {
            stmt.setInt(1, data.getFlightId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                availableSeats = rs.getInt("available_seats");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available seats: " + e.getMessage());
            return false;
        }

        System.out.println("\nSelect Class:");
        System.out.println("1. Economy");
        System.out.println("2. Business");
        System.out.println("3. First Class");
        System.out.print("Enter class choice: ");
        int classChoice = Integer.parseInt(scanner.nextLine());
        switch (classChoice) {
            case 1: data.setSeatClass("Economy"); break;
            case 2: data.setSeatClass("Business"); break;
            case 3: data.setSeatClass("First Class"); break;
            default: System.out.println("Invalid choice, defaulting to Economy."); data.setSeatClass("Economy"); break;
        }

        while (true) {
            System.out.printf("How many seats would you like to book? (Max available: %d): ", availableSeats);
            try {
                int seatsToBook = Integer.parseInt(scanner.nextLine());
                if (seatsToBook > 0 && seatsToBook <= availableSeats) {
                    data.setNumberOfSeats(seatsToBook);
                    break;
                } else {
                    System.out.println("❌ Invalid number of seats. Please enter a number between 1 and " + availableSeats + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
            }
        }

        System.out.println("\nSelect Seat Preference:");
        System.out.println("1. Window Seat");
        System.out.println("2. Any Random Seat");
        System.out.print("Enter your preference: ");
        int seatPreference = Integer.parseInt(scanner.nextLine());
        // *** EDITED CODE BLOCK START ***
        // Generate seat numbers for all booked seats
        String seatNumbers = generateSeatNumbers(data.getNumberOfSeats(), seatPreference);
        data.setSeatNumber(seatNumbers);
        // Update the confirmation message based on the number of seats
        if (data.getNumberOfSeats() > 1) {
            System.out.println("💺 Your seats have been automatically assigned: " + data.getSeatNumber());
        } else {
            System.out.println("💺 Your seat has been automatically assigned: " + data.getSeatNumber());
        }
        // *** EDITED CODE BLOCK END ***

        return true;
    }

    private static boolean collectPersonalInformation(Scanner scanner, BookingData data) {
        System.out.println("\n--- Step 2: Personal Information for " + data.getNumberOfSeats() + " Ticket(s) ---");

        int adults = 0, children = 0, infants = 0;

        while (true) {
            try {
                System.out.print("Enter number of Adults (age 12+): ");
                adults = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter number of Children (age 2-11): ");
                children = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter number of Infants (under 2, travels on lap): ");
                infants = Integer.parseInt(scanner.nextLine());

                if ((adults + children) != data.getNumberOfSeats()) {
                    System.out.println("❌ Error: The total of Adults + Children must equal the number of seats you booked (" + data.getNumberOfSeats() + "). Please try again.");
                    continue;
                }
                if (adults < 1) {
                    System.out.println("❌ Error: There must be at least one adult traveling.");
                    continue;
                }
                if (infants > adults) {
                    System.out.println("❌ Error: The number of infants cannot exceed the number of adults.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter numbers only.");
            }
        }

        data.getPassengers().clear();
        for (int i = 0; i < adults; i++) data.getPassengers().add(new PassengerInfo("Adult"));
        for (int i = 0; i < children; i++) data.getPassengers().add(new PassengerInfo("Child"));
        for (int i = 0; i < infants; i++) data.getPassengers().add(new PassengerInfo("Infant"));

        for (int i = 0; i < data.getPassengers().size(); i++) {
            PassengerInfo p = data.getPassengers().get(i);

            if (i == 0) {
                System.out.println("\n--- Primary Passenger Details (Adult 1) ---");
                System.out.print("Full Name (as per Passport/ID): ");
                // FIX: Trim the input to remove leading/trailing whitespace
                String primaryName = scanner.nextLine().trim();
                data.setFullName(primaryName);
                p.setFullName(primaryName);
            } else {
                System.out.printf("\n--- Details for %s %d ---\n", p.getPassengerType(), (i + 1));
                System.out.print("Full Name: ");
                // FIX: Trim the input here as well
                p.setFullName(scanner.nextLine().trim());
            }

            while (true) {
                System.out.print("Date of Birth (YYYY-MM-DD) for " + p.getFullName() + ": ");
                String dobStr = scanner.nextLine().trim();
                try {
                    LocalDate dob = LocalDate.parse(dobStr);
                    int age = Period.between(dob, LocalDate.now()).getYears();
                    boolean ageIsValid = false;

                    switch (p.getPassengerType()) {
                        case "Adult":
                            if (age >= 12) ageIsValid = true;
                            else System.out.println("❌ Error: Adult passengers must be 12 years or older.");
                            break;
                        case "Child":
                            if (age >= 2 && age < 12) ageIsValid = true;
                            else System.out.println("❌ Error: Child passengers must be between 2 and 11 years old.");
                            break;
                        case "Infant":
                            if (age < 2) ageIsValid = true;
                            else System.out.println("❌ Error: Infants must be under 2 years old.");
                            break;
                    }

                    if (ageIsValid) {
                        p.setDob(dobStr);
                        if (i == 0) data.setDob(dobStr);
                        break;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
                }
            }
        }

        System.out.println("\n--- Gender, Nationality & ID for Primary Passenger (" + data.getFullName() + ") ---");

        while (true) {
            System.out.print("Gender (1. Male, 2. Female, 3. Other): ");
            try {
                int genderChoice = Integer.parseInt(scanner.nextLine());
                if (genderChoice == 1) { data.setGender("Male"); break; }
                else if (genderChoice == 2) { data.setGender("Female"); break; }
                else if (genderChoice == 3) { data.setGender("Other"); break; }
                else { System.out.println("❌ Invalid choice. Please enter 1, 2, or 3."); }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
            }
        }

        System.out.print("Nationality: ");
        data.setNationality(scanner.nextLine());

        if (data.getFlightType().equals("International")) {
            System.out.print("Passport Number: ");
            data.setPassportNumber(scanner.nextLine());
            System.out.print("Passport Expiry Date (YYYY-MM-DD): ");
            String expiryDateStr = scanner.nextLine();
            try {
                LocalDate expiryDate = LocalDate.parse(expiryDateStr);
                if (expiryDate.isBefore(data.getDepartureDate())) {
                    System.out.println("Passport is expired or will expire before the flight.");
                    return false;
                }
                data.setPassportExpiry(expiryDateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                return false;
            }
        }
        return true;
    }

    private static boolean collectContactDetails(Scanner scanner, BookingData data) {
        System.out.println("\n--- Step 3: Contact Details for Primary Passenger ---");

        while (true) {
            System.out.print("Enter Phone Number: ");
            String phone = scanner.nextLine();
            if (phone.matches("^[6-9]\\d{9}$")) {
                data.setPhoneNumber(phone);
                break;
            } else {
                System.out.println("❌ Invalid phone number. Please ensure it is 10 digits and starts with 6, 7, 8, or 9.");
            }
        }

        while (true) {
            System.out.print("Email Address (e.g., example@gmail.com): ");
            String email = scanner.nextLine();
            if (email.toLowerCase().endsWith("@gmail.com") && email.indexOf('@') == email.lastIndexOf('@') && email.length() > 10) {
                data.setEmail(email);
                break;
            } else {
                System.out.println("❌ Invalid email format. It must end with '@gmail.com' OR '@GMAIL.COM'.");
            }
        }
        return true;
    }

    private static boolean processPayment(Scanner scanner, BookingData data) {
        System.out.println("\n--- Step 4: Payment Details ---");
        double adultFare = 0;
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT economy_fare, business_fare, first_class_fare FROM flights WHERE flight_id = ?")) {
            stmt.setInt(1, data.getFlightId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                switch (data.getSeatClass()) {
                    case "Economy": adultFare = rs.getDouble("economy_fare"); break;
                    case "Business": adultFare = rs.getDouble("business_fare"); break;
                    case "First Class": adultFare = rs.getDouble("first_class_fare"); break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching fare: " + e.getMessage());
            return false;
        }

        double totalFare = 0;
        System.out.println("\n--- Fare Breakdown ---");
        for (PassengerInfo p : data.getPassengers()) {
            double passengerFare = 0;
            if (p.getPassengerType().equals("Adult")) {
                passengerFare = adultFare;
                System.out.printf("1 x Adult Fare:        ₹%.2f\n", passengerFare);
            } else if (p.getPassengerType().equals("Child")) {
                passengerFare = adultFare * 0.75;
                System.out.printf("1 x Child Fare (25%% off): ₹%.2f\n", passengerFare);
            } else if (p.getPassengerType().equals("Infant")) {
                passengerFare = adultFare * 0.10;
                System.out.printf("1 x Infant Fare (90%% off): ₹%.2f\n", passengerFare);
            }
            totalFare += passengerFare;
        }
        data.setTotalFare(totalFare);
        System.out.println("-------------------------");

        System.out.printf("Total Amount for %d ticket(s) to be paid: ₹%.2f\n", data.getNumberOfSeats(), data.getTotalFare());

        while (true) {
            System.out.print("Enter 'pay' to confirm payment, or 'cancel' to abort: ");
            String confirmation = scanner.nextLine();
            if (confirmation.equalsIgnoreCase("pay")) {
                return true;
            }
            if (confirmation.equalsIgnoreCase("cancel")) {
                System.out.println("Booking process cancelled by user.");
                return false;
            }
            System.out.println("❌ Invalid command. Please try again.");
        }
    }

    private static void confirmAndSaveBooking(BookingData data) {
        String sql = "{CALL CreateBooking(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = DBConnection.getConnection();

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            // Ensure DOB is not null before proceeding
            if (data.getDob() == null || data.getDob().isEmpty()) {
                System.err.println("Booking failed: Date of Birth is missing for the primary passenger.");
                return;
            }

            cstmt.setString(1, data.getFullName());
            cstmt.setString(2, data.getGender());
            cstmt.setDate(3, java.sql.Date.valueOf(data.getDob()));
            cstmt.setString(4, data.getNationality());
            cstmt.setString(5, data.getPassportNumber());
            if (data.getPassportExpiry() != null) {
                cstmt.setDate(6, java.sql.Date.valueOf(data.getPassportExpiry()));
            } else {
                cstmt.setNull(6, java.sql.Types.DATE);
            }
            cstmt.setString(7, data.getPhoneNumber());
            cstmt.setString(8, data.getEmail());
            cstmt.setInt(9, data.getFlightId());
            cstmt.setString(10, data.getSeatClass());
            cstmt.setString(11, data.getSeatNumber());
            cstmt.setDouble(12, data.getTotalFare());
            cstmt.setInt(13, data.getNumberOfSeats());
            cstmt.registerOutParameter(14, java.sql.Types.INTEGER);

            cstmt.execute();

            int newBookingId = cstmt.getInt(14);

            System.out.println("\n✅ Booking Successful! Your Booking ID is: " + newBookingId);
            generateReceipt(data, newBookingId);

        } catch (SQLException e) {
            // FIX: Add specific handling for the "OUT parameter" error.
            if (e.getMessage().contains("is not an OUT parameter")) {
                System.err.println("\n❌ CRITICAL DATABASE ERROR: The application's database call does not match the 'CreateBooking' stored procedure in your database.");
                System.err.println("Please ensure your database has been updated with the latest SQL script, which removes the 'emergency_contact' field.");
            } else {
                System.err.println("Booking failed. A database error occurred: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Booking failed due to an invalid date format. Please ensure all dates are YYYY-MM-DD.");
        } catch (Exception e) {
            System.err.println("Booking failed. An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void generateReceipt(BookingData data, int bookingId) {
        String fileName = "Receipt_" + bookingId + "_" + data.getFullName().replace(" ", "_") + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            String flightNumber = "", departureCity = "", destinationCity = "", departureTime = "", arrivalTime = "";
            String sql = "SELECT flight_number, departure_city, destination_city, departure_time, arrival_time FROM flights WHERE flight_id = ?";
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, data.getFlightId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    flightNumber = rs.getString("flight_number");
                    departureCity = rs.getString("departure_city");
                    destinationCity = rs.getString("destination_city");
                    departureTime = rs.getTimestamp("departure_time").toString();
                    arrivalTime = rs.getTimestamp("arrival_time").toString();
                }
            }

            writer.write("========================================="); writer.newLine();
            writer.write("      AIRLINE TICKET & RECEIPT"); writer.newLine();
            writer.write("========================================="); writer.newLine();
            writer.newLine();
            writer.write("Booking ID: " + bookingId); writer.newLine();
            writer.write("Booking Date: " + LocalDate.now().toString()); writer.newLine();
            writer.newLine();
            writer.write("---------- PRIMARY CONTACT ----------"); writer.newLine();
            writer.write("Name: " + data.getFullName()); writer.newLine();
            writer.write("Email: " + data.getEmail()); writer.newLine();
            writer.write("Phone: " + data.getPhoneNumber()); writer.newLine();
            writer.newLine();
            writer.write("------------ PASSENGERS ------------"); writer.newLine();
            for (int i = 0; i < data.getPassengers().size(); i++) {
                PassengerInfo p = data.getPassengers().get(i);
                writer.write(String.format("%d. %-25s (%s)", (i + 1), p.getFullName(), p.getPassengerType()));
                writer.newLine();
            }
            writer.newLine();
            writer.write("----------- FLIGHT DETAILS ------------"); writer.newLine();
            writer.write("Flight Number: " + flightNumber); writer.newLine();
            writer.write("Route: " + departureCity + " to " + destinationCity); writer.newLine();
            writer.write("Departure: " + departureTime); writer.newLine();
            writer.write("Arrival:   " + arrivalTime); writer.newLine();
            writer.write("Class: " + data.getSeatClass()); writer.newLine();
            writer.write("Seat Number(s): " + data.getSeatNumber()); writer.newLine();
            writer.newLine();
            writer.write("------------ PAYMENT DETAILS ------------"); writer.newLine();
            writer.write(String.format("Total Amount Paid: ₹%.2f", data.getTotalFare())); writer.newLine();
            writer.write("Status: PAID"); writer.newLine();
            writer.newLine();
            writer.write("========================================="); writer.newLine();
            writer.write("    Thank you for flying with us!"); writer.newLine();
            writer.write("========================================="); writer.newLine();

            System.out.println("✅ Receipt generated successfully: " + fileName);

        } catch (IOException | SQLException e) {
            System.err.println("❌ Error: Could not generate receipt file. " + e.getMessage());
        }
    }

    // *** EDITED METHOD: Replaced generateSeatNumber with generateSeatNumbers ***
    /**
     * Generates a block of adjacent seat numbers.
     * @param numberOfSeats The number of seats to generate.
     * @param preference 1 for Window, 2 for Random.
     * @return A string of comma-separated seat numbers (e.g., "10A, 10B").
     */
    private static String generateSeatNumbers(int numberOfSeats, int preference) {
        Random random = new Random();
        int row = random.nextInt(40) + 1; // Rows 1-40
        char[] seatLetters = {'A', 'B', 'C', 'D', 'E', 'F'};
        List<String> assignedSeats = new ArrayList<>();

        if (numberOfSeats > 6) { // Cannot assign more than a full row
            // This is a fallback, in a real system you'd have more complex logic
            return "Not enough adjacent seats available.";
        }

        int startSeatIndex;

        if (preference == 1 && numberOfSeats > 0) { // Window preference
            // Try to get a block starting with A or F
            boolean startFromA = random.nextBoolean();
            if (startFromA) {
                startSeatIndex = 0; // Starts with 'A'
            } else {
                // If starting from F, calculate the start to fit all seats
                startSeatIndex = 6 - numberOfSeats; // e.g., for 3 seats, starts at D (index 3) to get D, E, F
            }
        } else { // Any random seat
            // Ensure the starting point allows all seats to fit in the row
            startSeatIndex = random.nextInt(7 - numberOfSeats); // e.g., for 3 seats, max start index is 3 (for C,D,E)
        }

        for (int i = 0; i < numberOfSeats; i++) {
            assignedSeats.add("" + row + seatLetters[startSeatIndex + i]);
        }

        return String.join(", ", assignedSeats);
    }
}
