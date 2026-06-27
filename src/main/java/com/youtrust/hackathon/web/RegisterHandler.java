package com.youtrust.hackathon.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.youtrust.hackathon.controller.RegisterInput;
import com.youtrust.hackathon.controller.UserController;
import com.youtrust.hackathon.model.AuthProvider;
import com.youtrust.hackathon.view.RegisterResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * POST /register を受け、JSON ボディを Controller に渡して JSON で返す HTTP ハンドラ。
 *
 * リクエスト: {"email","password","name","authProvider"}（authProviderは任意、既定PASSWORD）
 * レスポンス: {"success","userId","message"}
 */
public class RegisterHandler implements HttpHandler {

    private final UserController controller = new UserController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, "{\"success\":false,\"message\":\"POSTのみ対応\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> json = Json.parseFlatObject(body);

            RegisterInput input = new RegisterInput();
            input.setEmail(json.get("email"));
            input.setPassword(json.get("password"));
            input.setName(json.get("name"));
            String provider = json.get("authProvider");
            if (provider != null) {
                input.setAuthProvider(AuthProvider.valueOf(provider.toUpperCase()));
            }

            RegisterResult result = controller.register(input);
            respond(exchange, 200,
                "{\"success\":true,\"userId\":" + Json.quote(result.getUserId())
                    + ",\"message\":" + Json.quote(result.getMessage()) + "}");
        } catch (IllegalArgumentException e) {
            // バリデーション・重複・不正JSON・未知のauthProviderなど
            respond(exchange, 400, "{\"success\":false,\"message\":" + Json.quote(e.getMessage()) + "}");
        } catch (Exception e) {
            respond(exchange, 500,
                "{\"success\":false,\"message\":" + Json.quote("サーバエラー: " + e.getMessage()) + "}");
        }
    }

    private void respond(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
