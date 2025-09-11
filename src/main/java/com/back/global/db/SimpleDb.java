package com.back.global.db;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

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

    public void run(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            executeUpdate(pstmt, query, List.of(params));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    int executeUpdate(PreparedStatement pstmt, String query, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }

        if (devMode) log.trace("실행된 SQL문: {}", query);

        return pstmt.executeUpdate();
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Sql genSql() {
        return new Sql(this);
    }
}
