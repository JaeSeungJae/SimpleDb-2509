package com.back.global.db;

import java.sql.*;
import java.util.ArrayList;
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

        for (Object param : params) {
            this.params.add(param);
        }

        return this;
    }

    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sb.toString().trim(), Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();

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
}
