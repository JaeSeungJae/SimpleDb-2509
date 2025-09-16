package com.back.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SimpleDb {
    private final String dbHost;
    private final int dbPort;
    private final String dbUser;
    private final String dbPassword;
    private final String dbName;
    private boolean devMode = false;
    private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public SimpleDb(String dbHost, String dbUser, String dbPassword, String dbName) {
        this.dbHost = dbHost;
        this.dbPort = 3306; // default port
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
    }

    public SimpleDb(String dbHost, int dbPort, String dbUser, String dbPassword, String dbName) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbName = dbName;
    }

    private Connection getConnection() {
        try {
            Connection connection = connectionThreadLocal.get();
            if (connection == null) {
                connection = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbUser,
                        dbPassword);
                connection.setAutoCommit(true);
                connectionThreadLocal.set(connection);
            }
            return connection;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public void run(String stmt, Object... args) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(stmt);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            if (devMode) {
                System.out.println("SQL: " + stmt);
                System.out.println("Binded values: " + args);
            }
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Sql genSql() {
        try {
            Connection connection = getConnection();
            return new Sql(connection, devMode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            Connection connection = connectionThreadLocal.get();
            if (connection != null) {
                connection.close();
                connectionThreadLocal.remove();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void startTransaction() {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            Connection connection = getConnection();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
