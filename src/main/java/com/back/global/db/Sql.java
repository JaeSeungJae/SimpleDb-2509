package com.back.global.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
