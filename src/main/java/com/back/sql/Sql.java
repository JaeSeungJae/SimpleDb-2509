package com.back.sql;

import com.back.simpleDb.SimpleDb;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Sql {
    private final SimpleDb simpleDb;
    private final StringBuilder query = new StringBuilder();
    @Getter
    private final List<Object> params = new ArrayList<>();

    public Sql(SimpleDb simpleDb) {
        this.simpleDb = simpleDb;
    }

    public Sql append(String sqlPart) {
        query.append(" ").append(sqlPart);
        return this;
    }

    public Sql append(String sqlPart, Object param) {
        query.append(" ").append(sqlPart);
        params.add(param);
        return this;
    }



    private String getRawSql() {
        return query.toString();
    }

    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getRawSql(), Statement.RETURN_GENERATED_KEYS)) {

            // ? 값 바인딩
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1); // AUTO_INCREMENT 키 반환
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }


}
