package com.youtrust.hackathon.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.youtrust.hackathon.controller.RegisterInput;
import com.youtrust.hackathon.controller.UserController;
import com.youtrust.hackathon.view.RegisterResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * POST /register を受け、JSON ボディを Controller に渡して JSON で返す HTTP ハンドラ。
 *
 * リクエスト: {"email","password","name","authProvider"}
 *   authProvider は任意（既定 PASSWORD）。値は PASSWORD / GOOGLE / GITHUB のいずれか。
 * レスポンス: {"success","userId","message"}
 */
public class RegisterHandler implements HttpHandler {

    private final UserController controller = new UserController();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                respond(exchange, 405, new RegisterResult(false, null, "POSTのみ対応"));
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            RegisterInput input = gson.fromJson(body, RegisterInput.class);
            if (input == null) {
                throw new IllegalArgumentException("リクエストボディが空です");
            }

            RegisterResult result = controller.register(input);
            respond(exchange, 200, result);
        } catch (JsonSyntaxException e) {
            respond(exchange, 400, new RegisterResult(false, null, "JSONの形式が不正です"));
        } catch (IllegalArgumentException e) {
            // バリデーション・重複など
            respond(exchange, 400, new RegisterResult(false, null, e.getMessage()));
        } catch (Exception e) {
            respond(exchange, 500, new RegisterResult(false, null, "サーバエラー: " + e.getMessage()));
        }
    }

    private void respond(HttpExchange exchange, int status, RegisterResult result) throws IOException {
        byte[] bytes = gson.toJson(result).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
