package com.back.global.db;

import java.sql.*;
import java.util.*;

public class Sql {
    private final SimpleDb simpleDb;
    private final List<Object> params = new ArrayList<>();
    private final StringBuilder sb = new StringBuilder();

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String queryPart, Object... params) {
        sb.append(queryPart).append(" ");

        this.params.addAll(Arrays.asList(params));

        return this;
    }

    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS)) {

            simpleDb.executeUpdate(pstmt, sb.toString(), params);

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
             PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {

            return simpleDb.executeUpdate(pstmt, sb.toString(), params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int delete() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {

            return simpleDb.executeUpdate(pstmt, sb.toString(), params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, Object>> selectRows() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sb.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
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
}
