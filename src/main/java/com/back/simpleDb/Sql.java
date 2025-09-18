package com.back.simpleDb;

import java.time.LocalDateTime;
import java.util.*;

public class Sql {
    private final SimpleDb db;
    private final StringBuilder sb = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    public Sql(SimpleDb db) {
        this.db = db;
    }

    public Sql append(String piece) {
        if (!sb.isEmpty()) sb.append('\n');
        sb.append(piece);
        return this;
    }

    public Sql append(String piece, Object... params) {
        append(piece);
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    public Sql appendIn(String template, Object... values) {
        String placeholders = Arrays.asList(values).isEmpty()
                ? "NULL"
                : String.join(", ", Collections.nCopies(Arrays.asList(values).size(), "?"));
        String expanded = template.replaceFirst("\\?", placeholders);

        return append(expanded, values);
    }

    /* 실행 */
    public long insert() {
        return db.runInsertAndReturnKey(sb.toString(), params.toArray());
    }

    public int update() {
        return db.run(sb.toString(), params.toArray());
    }

    public int delete() {
        return db.run(sb.toString(), params.toArray());
    }

    /* 행 */
    public List<Map<String, Object>> selectRows() {
        return db.queryForList(sb.toString(), params.toArray());
    }

    public Map<String, Object> selectRow() {
        return db.queryForMap(sb.toString(), params.toArray());
    }

    public <T> List<T> selectRows(Class<T> type) {
        return selectRows().stream().map(row -> Convert.mapRowToPojo(row, type)).toList();
    }

    public <T> T selectRow(Class<T> type) {
        return Convert.mapRowToPojo(selectRow(), type);
    }

    /* 스칼라 */
    public boolean selectBoolean() {
        return Convert.convertScalar(selectRow(), Boolean.class);
    }

    public String selectString() {
        return Convert.convertScalar(selectRow(), String.class);
    }

    public LocalDateTime selectDatetime() {
        return Convert.convertScalar(selectRow(), LocalDateTime.class);
    }

    public Long selectLong() {
        return Convert.convertScalar(selectRow(), Long.class);
    }

    public List<Long> selectLongs() {
        return selectRows().stream().map(row -> Convert.convertScalar(row, Long.class)).toList();
    }
}
