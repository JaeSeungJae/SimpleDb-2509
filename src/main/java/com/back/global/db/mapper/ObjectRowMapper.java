package com.back.global.db.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ObjectRowMapper<T> implements BaseRowMapper<T> {
    private final Class<T> clazz;
    private final ResultSetMetaData metaData;
    private final int columnCount;

    public ObjectRowMapper(Class<T> clazz, ResultSetMetaData metaData) throws SQLException {
        this.clazz = clazz;
        this.metaData = metaData;
        this.columnCount = metaData.getColumnCount();
    }

    @Override
    public T mapRow(ResultSet rs) throws SQLException {
        try {
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("객체 매핑 실패: " + clazz.getName(), e);
        }
    }
}
