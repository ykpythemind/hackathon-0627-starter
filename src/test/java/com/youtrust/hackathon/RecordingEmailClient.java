package com.youtrust.hackathon;

import java.util.ArrayList;
import java.util.List;

/**
 * 送信内容を記録するだけのテスト用 {@link EmailClient}。
 */
class RecordingEmailClient implements EmailClient {

    static final class Sent {
        final String to;
        final String subject;
        final String body;

        Sent(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }
    }

    final List<Sent> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        sent.add(new Sent(to, subject, body));
    }
}
