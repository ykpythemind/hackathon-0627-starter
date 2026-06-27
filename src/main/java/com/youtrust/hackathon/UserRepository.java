package com.youtrust.hackathon;

/**
 * ユーザー永続化の抽象。実装を差し替えてテスト時にインメモリDBを使えるようにする。
 */
public interface UserRepository {

    /**
     * メールアドレスでユーザーを検索する。
     *
     * @return 見つからなければ null
     */
    User findByEmail(String email);

    /**
     * ユーザーを保存する。id が未設定なら採番する。
     */
    void save(User user);
}
