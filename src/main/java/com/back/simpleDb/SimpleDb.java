package com.back.simpleDb;

import lombok.Setter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SimpleDb {
    private final String host, username, password, dbName;
    private final ThreadLocal<Connection> connHolder = new ThreadLocal<>();
    @Setter
    private boolean devMode = false;

    public Sql genSql() {
        return new Sql(this);
    }

    public SimpleDb(String host, String username, String password, String dbName) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.dbName = dbName;
    }

    public Connection newConn() throws SQLException {
        String url = "jdbc:mysql://%s:3306/%s?serverTimezone=UTC&useUniCode=yes&characterEncoding=UTF-8".formatted(this.host, this.dbName);
        return DriverManager.getConnection(url, this.username, this.password);
    }

    private Connection acquire() throws SQLException {
        Connection c = connHolder.get();
        if (c != null) return c;
        c = newConn();
        c.setAutoCommit(true);
        connHolder.set(c);
        return c;
    }

    public void close() {
        Connection c = connHolder.get();
        if (c == null) return;
        try {
            c.close();
        } catch (SQLException ignored) {
        } finally {
            connHolder.remove();
        }
    }

    /* ==== 트랜잭션 ==== */
    public void startTransaction() {
        try {
            Connection c = acquire();
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        Connection c = connHolder.get();
        if (c == null) return;
        try {
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        Connection c = connHolder.get();
        if (c == null) return;
        try {
            c.rollback();
        } catch (SQLException ignored) {
        }
    }

    /* ==== 공통 exec 템플릿 ==== */
    @FunctionalInterface
    interface PsWork<R> {
        R apply(PreparedStatement ps) throws SQLException;
    }

    private <R> R exec(String sql, boolean returnKeys, PsWork<R> work, Object... params) {
        if (devMode) log(sql, params);
        try {
            Connection c = acquire();
            int mode = returnKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
            try (PreparedStatement ps = c.prepareStatement(sql, mode)) {
                bind(ps, params);
                return work.apply(ps);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL failed: " + e.getMessage(), e);
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof LocalDateTime t) ps.setTimestamp(i + 1, Timestamp.valueOf(t));
            else if (p instanceof Boolean b) ps.setBoolean(i + 1, b);
            else ps.setObject(i + 1, p);
        }
    }

    private void log(String sql, Object... params) {
        String out = sql;
        if (params != null) for (Object p : params) {
            out = out.replaceFirst("\\?", p.toString());
        }
        System.out.println("== rawSql ==\n" + out);
    }

    public int run(String sql, Object... params) {
        return exec(sql, false, PreparedStatement::executeUpdate, params); // DDL/UPDATE/DELETE
    }

    public long runInsertAndReturnKey(String sql, Object... params) {
        return exec(sql, true, ps -> {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }, params);
    }

    public List<Map<String, Object>> queryForList(String sql, Object... params) {
        return exec(sql, false, ps -> {
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }, params);
    }

    public Map<String, Object> queryForMap(String sql, Object... params) {
        List<Map<String, Object>> list = queryForList(sql, params);
        return list.isEmpty() ? null : list.getFirst();
    }

    private List<Map<String, Object>> mapRows(ResultSet rs) throws SQLException {
        var rows = new java.util.ArrayList<java.util.Map<String, Object>>();
        var md = rs.getMetaData();
        int n = md.getColumnCount();
        while (rs.next()) {
            var row = new java.util.LinkedHashMap<String, Object>();
            for (int i = 1; i <= n; i++) {
                Object v = rs.getObject(i);
                if (v instanceof byte[] b && b.length == 1) v = b[0] != 0; // BIT(1) 단순 보정
                row.put(md.getColumnLabel(i), v);
            }
            rows.add(row);
        }
        return rows;
    }
}
