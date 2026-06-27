package com.youtrust.hackathon.model;

/**
 * 認証方式。PASSWORD は従来のメール+パスワード登録、
 * それ以外は OAuth（authorization code grant 完了済み）での登録を表す。
 */
public enum AuthProvider {
    PASSWORD,
    GOOGLE,
    GITHUB;
}
