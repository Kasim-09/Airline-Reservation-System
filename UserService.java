package com.airline.Authentication;

import com.airline.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService implements IUserService {

    /**
     * UPDATED: Logs in a user by checking credentials against the 'passengers' table.
     * @return The passenger_id if successful, otherwise -1.
     */
    public int loginUserAndGetId(String username, String password) {
        // SQL query now targets the unified 'passengers' table.
        String sql = "SELECT passenger_id, password FROM passengers WHERE username = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // Check if the provided password matches the stored one.
                if (storedPassword.equals(password)) {
                    return rs.getInt("passenger_id"); // Return passenger_id on success
                }
            }
            return -1; // Return -1 if user not found or password incorrect
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            return -1;
        }
    }

    // This method is kept to satisfy the interface, but the one above is more useful.
    @Override
    public boolean loginUser(String username, String password) {
        return loginUserAndGetId(username, password) != -1;
    }


    /**
     * UPDATED: Registers a new user by inserting their details directly into the 'passengers' table.
     * Note: For a real-world app, more details like a default DOB or full name would be needed.
     */
    @Override
    public boolean registerUser(String username, String password, String email) {
        // This query now checks for existing username/email in the 'passengers' table.
        String checkUserSql = "SELECT passenger_id FROM passengers WHERE username = ? OR email = ?";
        // The INSERT statement now targets the 'passengers' table.
        // We add placeholder values for required fields like full_name and dob.
        String insertUserSql = "INSERT INTO passengers (username, password, email, full_name, dob, gender, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try {
            // Step 1: Check for duplicates.
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);
                if (checkStmt.executeQuery().next()) {
                    return false; // User already exists.
                }
            }

            // Step 2: Insert the new user with placeholder data.
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email);
                insertStmt.setString(4, "Default Name"); // Placeholder
                insertStmt.setDate(5, java.sql.Date.valueOf("2000-01-01")); // Placeholder
                insertStmt.setString(6, "Other"); // Placeholder
                insertStmt.setString(7, "0000000000"); // Placeholder
                return insertStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            return false;
        }
    }
}
