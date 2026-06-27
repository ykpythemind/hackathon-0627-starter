package com.youtrust.hackathon;

/**
 * メール送信の抽象。テスト時は送信内容を記録するフェイクに差し替える。
 */
public interface EmailClient {

    void send(String to, String subject, String body);
}
