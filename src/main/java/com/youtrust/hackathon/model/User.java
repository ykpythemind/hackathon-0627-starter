package com.youtrust.hackathon.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Model（Active Record）。ユーザーのデータと SQLite への永続化を1か所に持つ。
 * Repository 層は設けず、モデル自身が DB アクセスを担う。
 */
public class User {

    /** 接続は1本だけ保持して使い回す（インメモリDBは閉じると消えるため）。 */
    private static Connection connection;

    private String id;
    private String email;
    private String name;
    private String passwordHash;
    private AuthProvider authProvider;

    /** DB に接続しテーブルを用意する。アプリ起動時・テスト時に一度呼ぶ。 */
    public static void connect(String jdbcUrl) {
        try {
            connection = DriverManager.getConnection(jdbcUrl);
            try (Statement st = connection.createStatement()) {
                st.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id TEXT PRIMARY KEY," +
                    "  email TEXT NOT NULL UNIQUE," +
                    "  name TEXT NOT NULL," +
                    "  password_hash TEXT," +
                    "  auth_provider TEXT NOT NULL" +
                    ")"
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DBの初期化に失敗しました: " + jdbcUrl, e);
        }
    }

    /** メールアドレスでユーザーを検索する。見つからなければ null。 */
    public static User findByEmail(String email) {
        String sql = "SELECT id, email, name, password_hash, auth_provider FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.id = rs.getString("id");
                user.email = rs.getString("email");
                user.name = rs.getString("name");
                user.passwordHash = rs.getString("password_hash");
                user.authProvider = AuthProvider.valueOf(rs.getString("auth_provider"));
                return user;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("ユーザー検索に失敗しました: " + email, e);
        }
    }

    /** このユーザーを保存する。id が未設定なら採番する。 */
    public void save() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        String sql = "INSERT INTO users (id, email, name, password_hash, auth_provider) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, email);
            ps.setString(3, name);
            ps.setString(4, passwordHash);
            ps.setString(5, authProvider.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("ユーザー保存に失敗しました: " + email, e);
        }
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }
}
