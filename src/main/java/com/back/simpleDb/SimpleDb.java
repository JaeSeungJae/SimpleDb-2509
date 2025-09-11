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
}
