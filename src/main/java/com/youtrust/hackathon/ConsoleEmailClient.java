package com.youtrust.hackathon;

/**
 * 標準出力に送信内容を出すだけの {@link EmailClient}（簡易実装）。
 */
public class ConsoleEmailClient implements EmailClient {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("Email sent to: " + to);
    }
}
