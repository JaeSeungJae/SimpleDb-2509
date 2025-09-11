package com.back.global.db;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder queryBuilder = new StringBuilder();
    private final List<Object> queryParams = new ArrayList<>();

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String queryPart, Object... values) {
        queryBuilder.append(queryPart).append(" ");

        queryParams.addAll(Arrays.asList(values));

        return this;
    }

    public Sql appendIn(String queryPart, Object... values) {
        int idx = queryPart.indexOf("?");

        // (?, ?, ? ...)
        String placeholders = Arrays.stream(values)
                .map(v -> "?")
                .collect(Collectors.joining(", "));

        queryBuilder.append(queryPart.substring(0, idx))
                .append(placeholders)
                .append(queryPart.substring(idx + 1))
                .append(" ");

        Collections.addAll(queryParams, values);

        return this;
    }

    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {

            simpleDb.executeUpdate(pstmt, queryBuilder.toString(), queryParams);

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    public int update() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            return simpleDb.executeUpdate(pstmt, queryBuilder.toString(), queryParams);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int delete() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            return simpleDb.executeUpdate(pstmt, queryBuilder.toString(), queryParams);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> selectRows() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }

                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);

                        Field field;
                        try {
                            field = clazz.getDeclaredField(columnName);
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
                        field.setAccessible(true);

                        switch (value) {
                            case Timestamp ts when field.getType().equals(LocalDateTime.class) ->
                                    field.set(obj, ts.toLocalDateTime());
                            case Boolean b when field.getType().equals(boolean.class) -> field.set(obj, b);
                            default -> field.set(obj, value);
                        }
                    }

                    results.add(obj);
                }

                return results;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> selectRow() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }

                    return row;
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T selectRow(Class<T> clazz) {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);

                        Field field;
                        try {
                            field = clazz.getDeclaredField(columnName);
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
                        field.setAccessible(true);

                        switch (value) {
                            case Timestamp ts when field.getType().equals(LocalDateTime.class) ->
                                    field.set(obj, ts.toLocalDateTime());
                            case Boolean b when field.getType().equals(boolean.class) -> field.set(obj, b);
                            default -> field.set(obj, value);
                        }
                    }

                    return obj;
                }

                return null;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDateTime selectDatetime() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp(1);
                    if (timestamp != null) {
                        return timestamp.toLocalDateTime();
                    }
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long selectLong() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String selectString() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean selectBoolean() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }

                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> selectLongs() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < queryParams.size(); i++) {
                pstmt.setObject(i + 1, queryParams.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Long> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rs.getLong(1));
                }

                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
