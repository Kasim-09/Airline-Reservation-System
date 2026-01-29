package com.airline.Authentication;

import com.airline.Service.CustomerService;
import com.airline.ui.Customer;
import java.util.Scanner;

public class UserController {

    private static UserService userService = new UserService();
    private static CustomerService customerService = new CustomerService();

    public static void handleUserPanel(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Customer Login & Registration ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Back to Main Menu");
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
                    login(scanner);
                    break;
                case 2:
                    register(scanner);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void login(Scanner scanner) {
        System.out.println("\n--- Customer Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        for (int attempts = 1; attempts <= 3; attempts++) {
            System.out.print("Enter password (attempt " + attempts + "/3) or type 'exit' to go back: ");
            String password = scanner.nextLine();

            if (password.equalsIgnoreCase("exit")) {
                return;
            }

            // UPDATED: Use the new login method that returns an ID.
            int passengerId = userService.loginUserAndGetId(username, password);

            if (passengerId != -1) {
                System.out.println("\n✅ Login Successful! Welcome, " + username + ".");
                // We now have the passengerId directly, no need to look up a user_id.
                showCustomerDashboard(scanner, passengerId, username);
                return;
            } else {
                System.out.println("❌ Invalid password or username.");
            }
        }

        System.out.println("\nToo many failed login attempts. Redirecting to the registration page...");
        register(scanner);
    }

    // UPDATED: The dashboard now receives passengerId directly.
    private static void showCustomerDashboard(Scanner scanner, int passengerId, String username) {
        while (true) {
            System.out.println("\n--- Welcome " + username + " ---");
            System.out.println("1. Make a Reservation");
            System.out.println("2. View My Reservations");
            System.out.println("3. Cancel a Reservation");
            System.out.println("4. Logout");
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
                    Customer.handleCustomerMenu(scanner);
                    break;
                case 2:
                    // Pass the passengerId to view reservations.
                    customerService.viewMyReservations(passengerId);
                    break;
                case 3:
                    System.out.print("Enter the Booking ID to cancel: ");
                    try {
                        int bookingId = Integer.parseInt(scanner.nextLine());
                        // Pass the passengerId for cancellation verification.
                        if (customerService.cancelMyReservation(bookingId, passengerId)) {
                            System.out.println("✅ Booking " + bookingId + " has been successfully cancelled.");
                        } else {
                            System.out.println("❌ Cancellation failed.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid Booking ID. Please enter a number.");
                    }
                    break;
                case 4:
                    System.out.println("Logging you out. Goodbye, " + username + "!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void register(Scanner scanner) {
        System.out.println("\n--- New Customer Registration ---");
        System.out.print("Enter a new username: ");
        String username = scanner.nextLine();
        String email;
        while (true) {
            System.out.print("Enter an email address (must end with @gmail.com): ");
            email = scanner.nextLine();
            if (email.toLowerCase().endsWith("@gmail.com") && email.indexOf('@') == email.lastIndexOf('@') && email.length() > 10) {
                break;
            } else {
                System.out.println("❌ Invalid email format.");
            }
        }
        String password;
        while (true) {
            System.out.print("Enter a password (must be 8 or more characters): ");
            password = scanner.nextLine();
            if (password.length() >= 8) {
                break;
            } else {
                System.out.println("❌ Password is too short.");
            }
        }

        if (userService.registerUser(username, password, email)) {
            System.out.println("\n✅ Registration Successful! You can now log in.");
        } else {
            System.out.println("❌ Registration failed. The username or email may already be taken.");
        }
    }
}
