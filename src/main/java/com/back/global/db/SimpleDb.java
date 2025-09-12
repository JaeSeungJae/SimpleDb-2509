package com.back.global.db;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Setter
@Slf4j
public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private boolean devMode = false;

    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.user = user;
        this.password = password;
    }

    public Sql genSql() {
        return new Sql(this);
    }

    public void run(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            pstmt.executeUpdate();

            if (devMode) log.trace("실행된 SQL문: {}", query);
        } catch (SQLException e) {
            throw new RuntimeException("SQL문 실행 실패: " + query, e);
        }
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
