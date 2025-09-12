package com.back.global.db.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapRowMapper implements BaseRowMapper<Map<String, Object>> {
    private final ResultSetMetaData metaData;
    private final int columnCount;

    public MapRowMapper(ResultSetMetaData metaData) throws SQLException {
        this.metaData = metaData;
        this.columnCount = metaData.getColumnCount();
    }

    @Override
    public Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }

        return row;
    }
}
