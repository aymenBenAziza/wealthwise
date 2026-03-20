package com.wealthwise.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/wealthwise";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static DBConnection instance;
    private Connection connection;

    // ── Private constructor — Singleton ───────────────────────────────────────
    private DBConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ── Get single instance ───────────────────────────────────────────────────
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // ── Get connection ────────────────────────────────────────────────────────
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
        return connection;
    }
}