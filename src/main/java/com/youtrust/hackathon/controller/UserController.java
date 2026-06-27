package com.youtrust.hackathon.controller;

import com.youtrust.hackathon.model.AuthProvider;
import com.youtrust.hackathon.model.User;
import com.youtrust.hackathon.view.RegisterResult;

/**
 * Controller。リクエストを受けてバリデーション・認証方式の分岐を行い、
 * Model を保存して View（結果）を返す。
 */
public class UserController {

    /**
     * ユーザーを登録する。
     *
     * @throws IllegalArgumentException 入力が不正、またはメールが重複しているとき
     */
    public RegisterResult register(RegisterInput input) {
        AuthProvider provider = input.getAuthProvider() == null
            ? AuthProvider.PASSWORD
            : input.getAuthProvider();

        // 共通バリデーション
        if (input.getEmail() == null || !input.getEmail().contains("@")) {
            throw new IllegalArgumentException("メールアドレスが無効です");
        }
        if (input.getName() == null || input.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("名前は必須です");
        }

        // 認証方式で分岐。OAuth は authorization code grant が完了済みの前提なので、
        // パスワードは検証も保存もしない。
        String passwordHash = null;
        if (provider == AuthProvider.PASSWORD) {
            if (input.getPassword() == null || input.getPassword().length() < 8) {
                throw new IllegalArgumentException("パスワードは8文字以上必要です");
            }
            passwordHash = input.getPassword() + "_hashed"; // 簡略化。本番は bcrypt 等
        }

        if (User.findByEmail(input.getEmail()) != null) {
            throw new IllegalArgumentException("このメールアドレスはすでに登録されています");
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setName(input.getName());
        user.setPasswordHash(passwordHash);
        user.setAuthProvider(provider);
        user.save();

        sendWelcomeEmail(user);

        return new RegisterResult(true, user.getId(), "登録が完了しました");
    }

    private void sendWelcomeEmail(User user) {
        System.out.println("Email sent to: " + user.getEmail());
    }
}
