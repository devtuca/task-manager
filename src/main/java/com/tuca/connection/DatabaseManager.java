
package com.tuca.connection;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class DatabaseManager {

    private String url = "jdbc:mysql://localhost:3306/tasks?useSSL=false&serverTimezone=UTC";
    private String user = "root";
    private String password = "";
    private Connection connection;
    private final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {

            log.error(e.getMessage());

        }
    }

    public void execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            ps.execute();
        }
    }

    public <T> List<T> queryList(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        }

        return results;
    }


    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public void startConnection() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tarefas (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    description TEXT NOT NULL,
                    ownerName TEXT NOT NULL,
                    startDate BIGINT NOT NULL,
                    expiryDate BIGINT NOT NULL,
                    status VARCHAR(50) NOT NULL
                );
                """;

        try {
            execute(sql);
            log.info("Database connection established");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed");
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}