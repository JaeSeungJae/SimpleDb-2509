package com.back.global.db;

import lombok.Setter;

import java.sql.*;
import java.util.List;

@Setter
public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
    private boolean devMode = false;

    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.user = user;
        this.password = password;
    }

    public Sql genSql() {
        return new Sql(this, devMode);
    }

    public void run(String query, Object... params) {
        new QueryExecutor(this, devMode).executeUpdate(query, List.of(params), false);
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

    public void startTransaction() {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    public void commit() {
        try {
            Connection conn = getConnection();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("트랜젝션 커밋 실패", e);
        }
    }

    public void rollback() {
        try {
            Connection conn = getConnection();
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("트랜젝션 롤백 실패", e);
        }
    }

    public void close() {
        Connection conn = threadLocalConnection.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException("커넥션 닫기 실패", e);
            } finally {
                threadLocalConnection.remove();
            }
        } else {
            throw new RuntimeException("커넥션이 존재하지 않습니다.");
        }
    }
}
