package com.back.simpleDb;

import java.util.Map;

public class Convert {
    public static <T> T mapRowToPojo(Map<String, Object> row, Class<T> type) {
        try {
            T obj = type.getDeclaredConstructor().newInstance();
            if (row == null) return obj;

            for (var f : type.getDeclaredFields()) {
                Object v = row.get(f.getName());
                if (v == null) continue;
                f.setAccessible(true);
                f.set(obj, convert(v, f.getType()));
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertScalar(Map<String, Object> row, Class<T> type) {
        if (row == null) return null;
        Object v = row.values().stream().findFirst().orElse(null);
        return v != null ? convert(v, type) : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object v, Class<T> type) {
        if (v == null) return null;
        if (type.isInstance(v)) return (T) v;

        if (type == String.class) return (T) v.toString();

        if (type == Long.class || type == long.class) {
            return (T) ((v instanceof Number n) ? Long.valueOf(n.longValue())
                    : Long.valueOf(v.toString()));
        }

        if (type == Boolean.class || type == boolean.class) {
            return (T) switch (v) {
                case Boolean b -> b;
                case Number n -> Boolean.valueOf(n.intValue() != 0);
                case byte[] b -> Boolean.valueOf(b.length > 0 && b[0] != 0); // MySQL BIT(1)
                default -> Boolean.valueOf(v.toString());
            };
        }

        if (type == java.time.LocalDateTime.class) {
            if (v instanceof java.sql.Timestamp ts) return (T) ts.toLocalDateTime();
            if (v instanceof java.util.Date d) return (T) new java.sql.Timestamp(d.getTime()).toLocalDateTime();
            return (T) java.time.LocalDateTime.parse(v.toString()); // ISO 문자열 가정
        }

        // 필요 타입 생기면 여기에 한 줄씩 추가
        return (T) v;
    }
}
