package com.back.simpleDb;

import com.back.sql.Sql;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;

@Setter
@Getter
@AllArgsConstructor
public class SimpleDb {
    private final String url;
    private final String user;
    private final String password;
    private boolean devMode;
    private SimpleDb simpleDb;


    public SimpleDb(String host, String user, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":" + 3306 + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
        this.user = user;
        this.password = password;

    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    //SimpleDb에서는 실행, 커넥션 획득,드라이버 셋팅을 맡음
    public void run(String sql) {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String sql, Object... args) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Sql genSql() {
        return new Sql(this);
    }

    public void close() {

    }

    public void startTransaction() {

    }

    public void rollback() {
    }

    public void commit() {

    }


}