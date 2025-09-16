package com.back.simpleDb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sql {
    private boolean devMode = false;
    private Connection connection;
    private String sqlStatement;
    private List<Object> objects;
    private ObjectMapper objectMapper;

    public Sql(Connection connection) {
        this.connection = connection;
        this.sqlStatement = "";
        this.objects = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Sql(Connection connection, boolean devMode) {
        this(connection);
        this.devMode = devMode;
    }

    private void logSql() {
        if (devMode) {
            System.out.println("SQL: " + this.sqlStatement);
            System.out.println("Binded values: " + this.objects);
        }
    }

    public Sql append(String sqlPart, Object... args) {
        String currentSql = this.sqlStatement;
        String newSql = currentSql + " " + sqlPart;
        this.sqlStatement = newSql;
        for (Object arg : args) {
            this.objects.add(arg);
        }
        return this;
    }

    public Sql appendIn(String sqlPart, Object... args) {
        int idx = sqlPart.indexOf("?");
        if (idx == -1) {
            throw new IllegalArgumentException("No placeholder '?' found in sqlPart.");
        }

        if (args.length == 1 && args[0] instanceof Long[]) {
            Long[] longArray = (Long[]) args[0];
            args = new Object[longArray.length];
            for (int i = 0; i < longArray.length; i++) {
                args[i] = longArray[i];
            }
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            placeholders.append("?");
            if (i < args.length - 1) {
                placeholders.append(", ");
            }
        }

        String newSqlPart = sqlPart.replaceFirst("\\?", placeholders.toString());
        this.sqlStatement += " " + newSqlPart;
        for (Object arg : args) {
            this.objects.add(arg);
        }
        return this;
    }

    public long insert() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            if (devMode) {
                System.out.println("SQL: " + this.sqlStatement);
                System.out.println("Binded values: " + this.objects);
            }
            logSql();
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int update() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int delete() {
        return update();
    }

    private Map<String, Object> mapRow(ResultSet resultSet) {
        try {
            Map<String, Object> row = new HashMap<>();
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                row.put(columnName, columnValue);
            }
            return row;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> selectRows() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Map<String, Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = mapRow(resultSet);
                rows.add(row);
            }
            return rows;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> selectRow() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapRow(resultSet);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Long selectLong() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String selectString() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean selectBoolean() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> selectLongs() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Long> longs = new ArrayList<>();
            while (resultSet.next()) {
                longs.add(resultSet.getLong(1));
            }
            return longs;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDateTime selectDatetime() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                java.sql.Timestamp timestamp = resultSet.getTimestamp(1);
                if (timestamp != null) {
                    return timestamp.toLocalDateTime();
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> row = mapRow(resultSet);
                T obj = objectMapper.convertValue(row, clazz);
                results.add(obj);
            }
            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T selectRow(Class<T> clazz) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(this.sqlStatement);
            for (int i = 0; i < this.objects.size(); i++) {
                preparedStatement.setObject(i + 1, this.objects.get(i));
            }
            logSql();
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Map<String, Object> row = mapRow(resultSet);
                T obj = objectMapper.convertValue(row, clazz);
                return obj;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
