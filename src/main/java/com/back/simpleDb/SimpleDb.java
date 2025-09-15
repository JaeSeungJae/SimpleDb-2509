package com.back.simpleDb;

import com.back.sql.Sql;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SimpleDb {
    private final String localhost;
    private final String username;
    private final String password;
    private final String dbname;

    private Connection transactionConnection;

    public boolean isTransactionActive() {
        return transactionConnection != null;
    }

    public Connection getActiveConnection() throws SQLException {
        return isTransactionActive() ? transactionConnection : getConnection();
    }

    public SimpleDb(String localhost, String username, String password, String dbname) {
        this.localhost = localhost;
        this.username = username;
        this.password = password;
        this.dbname = dbname;
    }
    @Setter
    private boolean devMode;

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + localhost + ":3308/"
                + dbname + "?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, username, password);
    }

    public void run(String query) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            if (devMode) {
                System.out.println("Executed SQL: " + query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void run(String query, String title, String body, boolean isBlind) {
        try (Connection conn = getConnection();
             var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, body);
            pstmt.setBoolean(3, isBlind);
            pstmt.executeUpdate();
            if (devMode) {
                System.out.println("Executed SQL: " + query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Sql genSql() {
        return new Sql(this);
    }

    public void close() {}

    public void startTransaction() {
        if (transactionConnection != null) {
            System.err.println("Transaction has already been started.");
            return;
        }
        try {
            // Create a new connection and hold it
            transactionConnection = getConnection();
            // Disable auto-commit to manually control the transaction
            transactionConnection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start transaction", e);
        }
    }

    // New: Rolls back the current transaction
    public void rollback() {
        if (transactionConnection == null) {
            System.err.println("No active transaction to rollback.");
            return;
        }
        try {
            transactionConnection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        } finally {
            // End the transaction by closing the connection and resetting the state
            close(transactionConnection);
            transactionConnection = null;
        }
    }

    // New: Commits the current transaction (good practice to include)
    public void commit() {
        if (transactionConnection == null) {
            System.err.println("No active transaction to commit.");
            return;
        }
        try {
            transactionConnection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        } finally {
            // End the transaction by closing the connection and resetting the state
            close(transactionConnection);
            transactionConnection = null;
        }
    }

    private void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // It's common practice to ignore exceptions on close
                }
            }
        }
    }
}
