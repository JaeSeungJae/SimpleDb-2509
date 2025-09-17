package com.back.sql;

import com.back.simpleDb.SimpleDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

//문자열 조립을 위한 sql 클래스
public class Sql {
    private final StringBuilder builder;
    private final List<Object> params;
    private final SimpleDb simpleDb;

    public Sql(SimpleDb simpleDb) {
        builder = new StringBuilder();
        params = new ArrayList<>();
        this.simpleDb = simpleDb;
    }

    public Sql append(String sql, Object... args) {
        builder.append(" ").append(sql);
        params.addAll(List.of(args));
        return this;
    }

    public Sql appendIn(String sql, Object... args) {
        String placeholders = String.join(", ",
                Collections.nCopies(args.length, "?"));
        String expandedSql = sql.replace("?", placeholders);

        builder.append(" ").append(expandedSql);
        params.addAll(List.of(args));
        return this;
    }

    // 중복 코드 제거 로직
    public void bindParams(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    // 중복 코드 제거 로직 -2
    private Map<String, Object> extractRow(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }
        return row;
    }


    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int update() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int delete() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> selectRows() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = extractRow(rs);
                    results.add(row);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> selectRow() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        return null;
    }

    public <T> T selectRow(Class<T> clazz) {
        return null;
    }

    public LocalDateTime selectDatetime() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp(1).toLocalDateTime();
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long selectLong() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
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
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
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
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
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
             PreparedStatement stmt = conn.prepareStatement(builder.toString())) {
            bindParams(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
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