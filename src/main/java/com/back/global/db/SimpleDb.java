package com.back.global.db;

import lombok.Setter;

import java.sql.*;
import java.util.List;

@Setter
public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private boolean devMode = false;

    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.user = user;
        this.password = password;
    }

    public Sql genSql() {
        return new Sql(this, devMode);
    }

    Connection getConnection() throws SQLException {
        Connection conn = threadLocalConnection.get();
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(url, user, password);
                threadLocalConnection.set(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 실패", e);
        }
        return conn;
    }

    public void run(String query, Object... params) {
        new QueryExecutor(this, devMode).executeUpdate(query, List.of(params), false);
    }

    public void close() {
        Connection conn = threadLocalConnection.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException("Connection close 실패", e);
            } finally {
                threadLocalConnection.remove();
            }
        } else {
            throw new RuntimeException("Connection이 존재하지 않습니다.");
        }
    }
}
