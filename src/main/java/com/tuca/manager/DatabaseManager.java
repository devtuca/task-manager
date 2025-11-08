package com.tuca.manager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class DatabaseManager implements AutoCloseable {

    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initConnection();
    }

    public DatabaseManager() {
        this("jdbc:mysql://localhost:3306/tasks?useSSL=false&serverTimezone=UTC", "root", "");
    }

    public void initConnection() {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            log.info("Database connection established successfully");
        } catch (SQLException e) {
            log.error("Failed to establish database connection: {}", e.getMessage(), e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initConnection();
        }
        return connection;
    }

    @SneakyThrows
    public <K> Optional<K> query(String sql, Function<ResultSet, K> mapper, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(mapper.apply(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Query failed: {} - SQL: {}", e.getMessage(), sql, e);
            return Optional.empty();
        }
    }

    public <K> List<K> queryList(String sql, ResultSetMapper<K> mapper, Object... params) {
        List<K> results = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Query list failed: {} - SQL: {}", e.getMessage(), sql, e);
        }
        return results;
    }

    public int update(String sql, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Update failed: {} - SQL: {}", e.getMessage(), sql, e);
            return -1;
        }
    }

    public int insert(String sql, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(ps, params);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Insert failed: {} - SQL: {}", e.getMessage(), sql, e);
        }
        return 0;
    }

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public void initializeSchema() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS tarefas (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    description TEXT NOT NULL,
                    ownerName TEXT NOT NULL,
                    startDate BIGINT NOT NULL,
                    expiryDate BIGINT NOT NULL,
                    status VARCHAR(50) NOT NULL
                )
                """;

        int result = update(createTableSQL);
        if (result == -1) {
            log.warn("Table creation failed or table already exists");
        } else {
            log.info("Table 'tarefas' initialized successfully");
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            log.error("Error closing database connection: {}", e.getMessage(), e);
        }
    }
}