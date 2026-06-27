package com.youtrust.hackathon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * インメモリ SQLite に対して、永続化層も含めた登録フローを検証する。
 */
public class UserRegistrationServiceTest {

    private SqliteUserRepository repository;
    private RecordingEmailClient emailClient;
    private UserRegistrationService service;

    @Before
    public void setUp() {
        repository = new SqliteUserRepository("jdbc:sqlite::memory:");
        emailClient = new RecordingEmailClient();
        service = new UserRegistrationService(repository, emailClient);
    }

    @After
    public void tearDown() {
        repository.close();
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
        RegisterResult result = service.register(passwordInput());

        assertTrue(result.isSuccess());
        assertNotNull(result.getUserId());

        User saved = repository.findByEmail("alice@example.com");
        assertNotNull(saved);
        assertEquals("Alice", saved.getName());
        assertEquals(AuthProvider.PASSWORD, saved.getAuthProvider());
        assertEquals("password123_hashed", saved.getPasswordHash());
        assertEquals(1, emailClient.sent.size());
        assertEquals("alice@example.com", emailClient.sent.get(0).to);
    }

    @Test
    public void rejectsInvalidEmail() {
        RegisterInput input = passwordInput();
        input.setEmail("not-an-email");
        assertThrows(IllegalArgumentException.class, () -> service.register(input));
    }

    @Test
    public void rejectsBlankName() {
        RegisterInput input = passwordInput();
        input.setName("  ");
        assertThrows(IllegalArgumentException.class, () -> service.register(input));
    }

    @Test
    public void rejectsShortPasswordForPasswordProvider() {
        RegisterInput input = passwordInput();
        input.setPassword("short");
        assertThrows(IllegalArgumentException.class, () -> service.register(input));
    }

    @Test
    public void rejectsDuplicateEmail() {
        service.register(passwordInput());
        assertThrows(IllegalArgumentException.class, () -> service.register(passwordInput()));
    }

    @Test
    public void registersOAuthUserWithoutPassword() {
        RegisterInput input = new RegisterInput();
        input.setEmail("bob@example.com");
        input.setName("Bob");
        input.setAuthProvider(AuthProvider.GOOGLE);

        RegisterResult result = service.register(input);

        assertTrue(result.isSuccess());
        User saved = repository.findByEmail("bob@example.com");
        assertNotNull(saved);
        assertEquals(AuthProvider.GOOGLE, saved.getAuthProvider());
        assertNull("OAuth登録ではパスワードを保存しない", saved.getPasswordHash());
        assertEquals(1, emailClient.sent.size());
    }

    @Test
    public void oauthSkipsPasswordValidation() {
        // 短すぎるパスワードでも OAuth では検証されず登録できる
        RegisterInput input = new RegisterInput();
        input.setEmail("carol@example.com");
        input.setName("Carol");
        input.setAuthProvider(AuthProvider.GITHUB);
        input.setPassword("x");

        RegisterResult result = service.register(input);

        assertTrue(result.isSuccess());
        assertNull(repository.findByEmail("carol@example.com").getPasswordHash());
    }

    @Test
    public void oauthStillRequiresValidEmail() {
        RegisterInput input = new RegisterInput();
        input.setEmail("invalid");
        input.setName("Dave");
        input.setAuthProvider(AuthProvider.GOOGLE);
        assertThrows(IllegalArgumentException.class, () -> service.register(input));
    }
}
