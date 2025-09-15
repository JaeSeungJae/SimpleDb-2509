package com.back.simpleDb;

import com.back.sql.Sql;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private boolean devMode;


    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":" + 3306 + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
        this.user = user;
        this.password = password;

    }

    public Connection getConnection() throws SQLException {
        return null;
    }

    public void run(String sql) {

    }

    public void run(String sql, Object... args) {

    }

    public void setDevMode(boolean b) {

    }


    public Sql genSql() {
        return null;
    }

    public void close() {

    }

    public void startTransaction() {

    }

    public void rollback() {
    }

    public void commit() {

    }
}