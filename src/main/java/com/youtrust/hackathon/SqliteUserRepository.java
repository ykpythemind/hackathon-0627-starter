package com.youtrust.hackathon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * SQLite を使った {@link UserRepository} 実装。
 *
 * インメモリDB（jdbc:sqlite::memory:）は接続を閉じると消えるため、
 * 接続をインスタンス内で1本保持して使い回す。利用後は {@link #close()} する。
 */
public class SqliteUserRepository implements UserRepository, AutoCloseable {

    private final Connection connection;

    public SqliteUserRepository(String jdbcUrl) {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            initSchema();
        } catch (SQLException e) {
            throw new IllegalStateException("DBの初期化に失敗しました: " + jdbcUrl, e);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id TEXT PRIMARY KEY," +
                "  email TEXT NOT NULL UNIQUE," +
                "  name TEXT NOT NULL," +
                "  password_hash TEXT" +
                ")"
            );
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, email, name, password_hash FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getString("id"));
                user.setEmail(rs.getString("email"));
                user.setName(rs.getString("name"));
                user.setPasswordHash(rs.getString("password_hash"));
                return user;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("ユーザー検索に失敗しました: " + email, e);
        }
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO users (id, email, name, password_hash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getName());
            ps.setString(4, user.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("ユーザー保存に失敗しました: " + user.getEmail(), e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            // クローズ失敗は致命的でないため無視する
        }
    }
}
