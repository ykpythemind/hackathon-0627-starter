package com.youtrust.hackathon.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.youtrust.hackathon.model.AuthProvider;
import com.youtrust.hackathon.model.User;
import com.youtrust.hackathon.view.RegisterResult;
import org.junit.Before;
import org.junit.Test;

/**
 * インメモリ SQLite に対して、Model も含めた登録フローを検証する。
 */
public class UserControllerTest {

    private UserController controller;

    @Before
    public void setUp() {
        // 各テストで新しいインメモリDBに接続するため、毎回まっさらな状態になる。
        User.connect("jdbc:sqlite::memory:");
        controller = new UserController();
    }

    private static RegisterInput passwordInput() {
        RegisterInput input = new RegisterInput();
        input.setEmail("alice@example.com");
        input.setName("Alice");
        input.setPassword("password123");
        return input;
    }

    @Test
    public void registersUserWithPasswordAndPersists() {
        RegisterResult result = controller.register(passwordInput());

        assertTrue(result.isSuccess());
        assertNotNull(result.getUserId());

        User saved = User.findByEmail("alice@example.com");
        assertNotNull(saved);
        assertEquals("Alice", saved.getName());
        assertEquals(AuthProvider.PASSWORD, saved.getAuthProvider());
        assertEquals("password123_hashed", saved.getPasswordHash());
    }

    @Test
    public void rejectsInvalidEmail() {
        RegisterInput input = passwordInput();
        input.setEmail("not-an-email");
        assertThrows(IllegalArgumentException.class, () -> controller.register(input));
    }

    @Test
    public void rejectsBlankName() {
        RegisterInput input = passwordInput();
        input.setName("  ");
        assertThrows(IllegalArgumentException.class, () -> controller.register(input));
    }

    @Test
    public void rejectsShortPasswordForPasswordProvider() {
        RegisterInput input = passwordInput();
        input.setPassword("short");
        assertThrows(IllegalArgumentException.class, () -> controller.register(input));
    }

    @Test
    public void rejectsDuplicateEmail() {
        controller.register(passwordInput());
        assertThrows(IllegalArgumentException.class, () -> controller.register(passwordInput()));
    }

    @Test
    public void registersOAuthUserWithoutPassword() {
        RegisterInput input = new RegisterInput();
        input.setEmail("bob@example.com");
        input.setName("Bob");
        input.setAuthProvider(AuthProvider.GOOGLE);

        RegisterResult result = controller.register(input);

        assertTrue(result.isSuccess());
        User saved = User.findByEmail("bob@example.com");
        assertNotNull(saved);
        assertEquals(AuthProvider.GOOGLE, saved.getAuthProvider());
        assertNull("OAuth登録ではパスワードを保存しない", saved.getPasswordHash());
    }

    @Test
    public void oauthSkipsPasswordValidation() {
        // 短すぎるパスワードでも OAuth では検証されず登録できる
        RegisterInput input = new RegisterInput();
        input.setEmail("carol@example.com");
        input.setName("Carol");
        input.setAuthProvider(AuthProvider.GITHUB);
        input.setPassword("x");

        RegisterResult result = controller.register(input);

        assertTrue(result.isSuccess());
        assertNull(User.findByEmail("carol@example.com").getPasswordHash());
    }

    @Test
    public void oauthStillRequiresValidEmail() {
        RegisterInput input = new RegisterInput();
        input.setEmail("invalid");
        input.setName("Dave");
        input.setAuthProvider(AuthProvider.GOOGLE);
        assertThrows(IllegalArgumentException.class, () -> controller.register(input));
    }
}
