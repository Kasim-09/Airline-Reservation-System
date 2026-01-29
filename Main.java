
package com.airline.main;

import com.airline.Authentication.UserController;
import com.airline.database.DBConnection;
import com.airline.ui.Admin;
import com.airline.ui.CompanyController;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("✈️  Welcome to the Airline Reservation System! ✈️");
        System.out.println("==============================================");

        if (DBConnection.getConnection() == null) {
            System.out.println("Failed to connect to the database. Exiting application.");
            return;
        }

        while (true) {
            System.out.println("\nWho are you?");
            System.out.println("1. Customer");
            System.out.println("2. Admin");
            System.out.println("3. Airplane Company");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        UserController.handleUserPanel(scanner);
                        break;
                    case 2:
                        Admin.handleAdminMenu(scanner);
                        break;
                    case 3:
                        CompanyController.handleCompanyPanel(scanner);
                        break;
                    case 4:
                        System.out.println("Thank you for using the Airline Reservation System. Goodbye!");
                        DBConnection.closeConnection();
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }
}





