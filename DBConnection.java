package com.airline.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/airline_db";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Connection connection = null;

    private DBConnection() {} // Private constructor to prevent instantiation

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Register the JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error connecting to the database: " + e.getMessage());
                System.exit(1); // Exit if database connection failed
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}