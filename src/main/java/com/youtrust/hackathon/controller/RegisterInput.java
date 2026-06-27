package com.youtrust.hackathon.controller;

import com.youtrust.hackathon.model.AuthProvider;

/**
 * 登録リクエストの入力値。
 */
public class RegisterInput {

    private String email;
    private String password;
    private String name;
    /** 認証方式。未指定なら従来どおりパスワード登録として扱う。 */
    private AuthProvider authProvider = AuthProvider.PASSWORD;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }
}
