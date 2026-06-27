package com.youtrust.hackathon;

import java.util.logging.Logger;

/**
 * ユーザー登録のオーケストレーション。
 *
 * バリデーション・永続化・メール送信を協調させる。永続化とメール送信は
 * コンストラクタで注入し、テスト時に差し替えられるようにする。
 */
public class UserRegistrationService {

    private static final Logger logger = Logger.getLogger(UserRegistrationService.class.getName());

    private final UserRepository userRepository;
    private final EmailClient emailClient;

    public UserRegistrationService(UserRepository userRepository, EmailClient emailClient) {
        this.userRepository = userRepository;
        this.emailClient = emailClient;
    }

    /**
     * ユーザーを登録する。
     *
     * @throws IllegalArgumentException 入力が不正、またはメールが重複しているとき
     */
    public RegisterResult register(RegisterInput input) {
        validate(input);

        if (userRepository.findByEmail(input.getEmail()) != null) {
            throw new IllegalArgumentException("このメールアドレスはすでに登録されています");
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setName(input.getName());
        user.setPasswordHash(hashPassword(input.getPassword()));
        userRepository.save(user);

        sendWelcomeEmail(user);
        logger.info("ユーザー登録完了: " + user.getEmail());

        return new RegisterResult(true, user.getId(), "登録が完了しました");
    }

    private void validate(RegisterInput input) {
        if (input.getEmail() == null || !input.getEmail().contains("@")) {
            throw new IllegalArgumentException("メールアドレスが無効です");
        }
        if (input.getName() == null || input.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("名前は必須です");
        }
        if (input.getPassword() == null || input.getPassword().length() < 8) {
            throw new IllegalArgumentException("パスワードは8文字以上必要です");
        }
    }

    private void sendWelcomeEmail(User user) {
        String subject = "【ハッカソン】登録完了のお知らせ";
        String body = user.getName() + " 様\n\nご登録ありがとうございます。";
        emailClient.send(user.getEmail(), subject, body);
    }

    private String hashPassword(String rawPassword) {
        // 簡略化。本番では bcrypt/argon2 等を使う
        return rawPassword + "_hashed";
    }
}
