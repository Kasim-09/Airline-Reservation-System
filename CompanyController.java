package com.airline.ui;

import com.airline.Service.CompanyService;
import com.airline.database.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class CompanyController {

    private static CompanyService companyService = new CompanyService();

    public static void handleCompanyPanel(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Airline Company Portal ---");
            System.out.println("1. Company Login");
            System.out.println("2. Register New Company");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                // This line will now be safely handled
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                // This block runs if the user enters text instead of a number
                System.out.println("❌ Invalid input. Please enter a number (1, 2, or 3).");
                continue; // The loop will restart and ask for input again
            }

            switch (choice) {
                case 1:
                    login(scanner);
                    break;
                case 2:
                    register(scanner);
                    break;
                case 3:
                    return; // Go back to the main menu
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            }
        }
    }

    private static void login(Scanner scanner) {
        System.out.println("\n--- Company Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        int companyId = companyService.loginCompany(username, password);
        if (companyId != -1) {
            String companyName = companyService.getCompanyName(companyId);
            System.out.println("\n✅ Login Successful! Welcome, " + companyName + ".");
            showCompanyDashboard(scanner, companyId, companyName);
        } else {
            System.out.println("❌ Invalid username or password.");
        }
    }

    private static void register(Scanner scanner) {
        System.out.println("\n--- New Company Registration ---");
        System.out.print("Enter your official company name: ");
        String companyName = scanner.nextLine();
        System.out.print("Enter a new username for your admin: ");
        String username = scanner.nextLine();
        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        if (companyService.registerCompany(companyName, username, password)) {
            System.out.println("\n✅ Company registered successfully! You can now log in.");
        } else {
            System.out.println("❌ Registration failed. Company name or username may already exist.");
        }
    }

    private static void showCompanyDashboard(Scanner scanner, int companyId, String companyName) {
        while (true) {
            System.out.println("\n--- " + companyName + " Dashboard ---");
            System.out.println("1. Add New Aircraft to Your Fleet");
            System.out.println("2. View Your Scheduled Flights");
            System.out.println("3. Ban an Aircraft");
            System.out.println("4. Unban an Aircraft"); // <<< NEW OPTION
            System.out.println("5. Logout"); // Renumbered
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
                    companyService.addAircraft(scanner, companyId, companyName);
                    break;
                case 2:
                    companyService.viewScheduledFlights(companyId);
                    break;
                case 3:
                    companyService.banAircraft(scanner, companyId);
                    break;
                case 4:
                    companyService.unbanAircraft(scanner, companyId); // <<< NEW METHOD CALL
                    break;
                case 5:
                    return; // Logout
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    /**
     * Unbans an aircraft by updating its status back to 'Active'.
     */

}