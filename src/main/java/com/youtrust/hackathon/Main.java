package com.youtrust.hackathon;

import com.sun.net.httpserver.HttpServer;
import com.youtrust.hackathon.model.User;
import com.youtrust.hackathon.web.RegisterHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * アプリのエントリポイント。SQLite に接続し、JDK 標準の HttpServer を起動する。
 * 接続先とポートは環境変数 DB_URL / PORT で上書きできる。
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:sqlite:users.db");
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        User.connect(dbUrl);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/register", new RegisterHandler());
        server.createContext("/health", exchange -> {
            byte[] ok = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, ok.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(ok);
            }
        });
        server.start();

        System.out.println("Listening on http://localhost:" + port
            + "  (POST /register, GET /health)  db=" + dbUrl);
    }
}
