package com.back.sql;

import com.back.Article;
import com.back.simpleDb.SimpleDb;
import lombok.Getter;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    public Sql append(String sqlPart, Object... params) {
        query.append(" ").append(sqlPart);

        if (params != null) {
            for (Object param : params) {
                this.params.add(param);
            }
        }
        return this;
    }

    public Sql appendIn(String sqlPart, Object... params) {
        if (params == null || params.length == 0) {
            throw new IllegalArgumentException("params is null or empty");
        }
        String placeholders = String.join(", ", Collections.nCopies(params.length, "?"));
        String replacedSql = sqlPart.replace("?", placeholders);
        query.append(" ").append(replacedSql);
        for (Object param : params) {
            this.params.add(param);
        }
        return this;
    }



    private String getRawSql() {
        return query.toString();
    }

    private ResultSet executeQueryWithResultSet(String sql) throws SQLException {
        Connection conn = simpleDb.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
        return pstmt.executeQuery();
    }

    // ResultSet을 Map으로 변환하는 공통 메서드
    private List<Map<String, Object>> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    // ResultSet의 첫 번째 행을 Map으로 변환하는 공통 메서드
    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        if (rs.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
        }
        return row;
    }

    private List<Article> articleResultSetToList(ResultSet rs) throws SQLException {
        List<Article> row = new ArrayList<>();
        while (rs.next()) {
            Article article = new Article();
            article.setId(rs.getLong("id"));
            article.setCreatedDate((rs.getTimestamp("createdDate").toLocalDateTime()));
            article.setModifiedDate((rs.getTimestamp("modifiedDate").toLocalDateTime()));
            article.setTitle(rs.getString("title"));
            article.setBody(rs.getString("body"));
            article.setBlind(rs.getBoolean("isBlind"));
        }
        return row;
    }

    private Article articleResultSetToArticle(ResultSet rs) throws SQLException {
        Article article = new Article();
        if (rs.next()) {
            article.setId(rs.getLong("id"));
            article.setCreatedDate((rs.getTimestamp("createdDate").toLocalDateTime()));
            article.setModifiedDate((rs.getTimestamp("modifiedDate").toLocalDateTime()));
            article.setTitle(rs.getString("title"));
            article.setBody(rs.getString("body"));
            article.setBlind(rs.getBoolean("isBlind"));
        }
        return article;
    }

    private List<String> getSelectColumns() {
        String rawSql = getRawSql();
        String upperSql = rawSql.toUpperCase();

        int selectIdx = upperSql.indexOf("SELECT");
        int fromIdx = upperSql.indexOf("FROM");

        if (selectIdx == -1 || fromIdx == -1 || fromIdx <= selectIdx) {
            return List.of(); // SELECT ~ FROM 구간이 없을 때
        }

        String selectClause = rawSql.substring(selectIdx + 6, fromIdx).trim();

        return Arrays.stream(selectClause.split(","))
                .map(String::trim)
                .toList();
    }

    public long insert() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getRawSql(), Statement.RETURN_GENERATED_KEYS)) {

            // ? 값 바인딩
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = pstmt.executeUpdate(); // INSERT 문 실행
            if (affectedRows == 0) {
                throw new SQLException("Inserting data failed, no rows affected.");
            }

            // 자동 생성된 키를 반환
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1); // 첫 번째 컬럼 (자동 증가된 키)
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1; // 실패 시 -1 반환
    }

    // update 메서드
    public int update() {
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(getRawSql())) {
            // ? 값 바인딩
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            return pstmt.executeUpdate(); // update, delete는 영향받은 수만큼이 return 값임
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // delete 메서드
    public int delete() {
        return update();
    }

    // selectRows 메서드
    public List<Map<String, Object>> selectRows() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Article> selectRows(Object obj) {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            return articleResultSetToList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // selectRow 메서드
    public Map<String, Object> selectRow() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            return mapResultSetToMap(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Article selectRow(Object obj) {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            return articleResultSetToArticle(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // selectDatetime 메서드
    public LocalDateTime selectDatetime() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            if (rs.next()) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Long selectLong() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            if (rs.next()) {
                List<String> columns = getSelectColumns();
                return rs.getLong(columns.get(0));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1L;
    }

    public String selectString() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean selectBoolean() {
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            if (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                boolean isExist = false;
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    if (meta.getColumnName(i).equalsIgnoreCase("isBlind")) { isExist = true; break; }
                }
                if (isExist) { return rs.getBoolean("isBlind"); }
                else { return rs.getBoolean(1); }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public List<Long> selectLongs() {
        List<Long> rows = new ArrayList<>();
        try (ResultSet rs = executeQueryWithResultSet(getRawSql())) {
            while (rs.next()) {
                rows.add(rs.getLong("id"));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
