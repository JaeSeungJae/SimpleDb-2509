package com.back.global.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface BaseRowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException, ReflectiveOperationException;
}
