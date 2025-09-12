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

    public Sql genSql() {
        return new Sql(this);
    }

    public void run(String query, Object... params) {
        new QueryExecutor(this).executeUpdate(query, List.of(params), false);
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
