package com.back.global.db;

import com.back.global.db.mapper.MapRowMapper;
import com.back.global.db.mapper.ObjectRowMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Sql {
    private final QueryExecutor executor;
    private final StringBuilder sqlQuery = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();

    public Sql(SimpleDb simpleDb) {
        this.executor = new QueryExecutor(simpleDb);
    }

    public Sql append(String queryPart, Object... values) {
        sqlQuery.append(queryPart).append(" ");

        parameters.addAll(Arrays.asList(values));

        return this;
    }

    public Sql appendIn(String queryPart, Object... values) {
        int idx = queryPart.indexOf("?");

        // (?, ?, ?, ...)
        String placeholders = Arrays.stream(values)
                .map(v -> "?")
                .collect(Collectors.joining(", "));

        sqlQuery.append(queryPart, 0, idx)
                .append(placeholders)
                .append(queryPart.substring(idx + 1))
                .append(" ");

        Collections.addAll(parameters, values);

        return this;
    }

    public long insert() {
        return executor.executeUpdate(sqlQuery.toString(), parameters, true);
    }

    public int update() {
        return (int) executor.executeUpdate(sqlQuery.toString(), parameters, false);
    }

    public int delete() {
        return (int) executor.executeUpdate(sqlQuery.toString(), parameters, false);
    }

    public List<Map<String, Object>> selectRows() {
        return executor.executeSelectList(sqlQuery.toString(), parameters, rs -> new MapRowMapper(rs.getMetaData()).mapRow(rs));
    }

    public Map<String, Object> selectRow() {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> new MapRowMapper(rs.getMetaData()).mapRow(rs));
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        return executor.executeSelectList(sqlQuery.toString(), parameters, rs -> new ObjectRowMapper<>(clazz, rs.getMetaData()).mapRow(rs));
    }

    public <T> T selectRow(Class<T> clazz) {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> new ObjectRowMapper<>(clazz, rs.getMetaData()).mapRow(rs));
    }

    public LocalDateTime selectDatetime() {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> {
            Timestamp ts = rs.getTimestamp(1);
            return ts != null ? ts.toLocalDateTime() : null;
        });
    }

    public Long selectLong() {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> rs.getLong(1));
    }

    public String selectString() {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> rs.getString(1));
    }

    public Boolean selectBoolean() {
        return executor.executeSelectOne(sqlQuery.toString(), parameters, rs -> rs.getBoolean(1));
    }

    public List<Long> selectLongs() {
        return executor.executeSelectList(sqlQuery.toString(), parameters, rs -> rs.getLong(1));
    }
}
