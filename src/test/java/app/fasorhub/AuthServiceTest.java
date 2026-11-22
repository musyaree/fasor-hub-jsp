package app.fasorhub;

import app.fasorhub.exception.ValidationException;
import app.fasorhub.model.Role;
import app.fasorhub.model.User;
import app.fasorhub.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuthService covering AUT-01 through AUT-05.
 */
class AuthServiceTest {

    @BeforeEach
    void setUp() throws Exception {
        TestDb.fresh(false);
    }

    // AUT-01: Registration with valid data creates a borrower
    @Test
    void registerCreatesNewBorrower() throws Exception {
        User user = AuthService.register(
                "Test User",
                "test@example.com",
                "password123",
                "12345678",
                "08123456789"
        );

        assertNotNull(user);
        assertEquals("Test User", user.name());
        assertEquals("test@example.com", user.email());
        assertEquals(Role.BORROWER, user.role());
        assertNull(user.passwordHash()); // Password should not be in returned user
    }

    // AUT-02: Registration with duplicate email fails with P22
    @Test
    void registerDuplicateEmailThrowsP22() throws Exception {
        // First registration
        AuthService.register("User 1", "duplicate@example.com", "pass1", "111", "081");

        // Second registration with same email
        ValidationException ex = assertThrows(ValidationException.class, () ->
                AuthService.register("User 2", "duplicate@example.com", "pass2", "222", "082")
        );

        assertEquals("p22", ex.getMessageKey());
    }

    // AUT-03: Registration with missing fields fails with P23
    @Test
    void registerMissingFieldsThrowsP23() {
        // Empty name
        ValidationException ex1 = assertThrows(ValidationException.class, () ->
                AuthService.register("", "test@example.com", "pass", "id", "phone")
        );
        assertEquals("p23", ex1.getMessageKey());

        // Empty email
        ValidationException ex2 = assertThrows(ValidationException.class, () ->
                AuthService.register("Name", "", "pass", "id", "phone")
        );
        assertEquals("p23", ex2.getMessageKey());

        // Empty password
        ValidationException ex3 = assertThrows(ValidationException.class, () ->
                AuthService.register("Name", "test@example.com", "", "id", "phone")
        );
        assertEquals("p23", ex3.getMessageKey());

        // Whitespace-only name
        ValidationException ex4 = assertThrows(ValidationException.class, () ->
                AuthService.register("   ", "test@example.com", "pass", "id", "phone")
        );
        assertEquals("p23", ex4.getMessageKey());

        // Null identity
        ValidationException ex5 = assertThrows(ValidationException.class, () ->
                AuthService.register("Name", "test@example.com", "pass", null, "phone")
        );
        assertEquals("p23", ex5.getMessageKey());
    }

    // AUT-04: Login with valid credentials returns user
    @Test
    void loginWithValidCredentials() throws Exception {
        // Register first
        AuthService.register("Login Test", "login@example.com", "correctpass", "123", "081");

        // Login
        User user = AuthService.login("login@example.com", "correctpass");

        assertNotNull(user);
        assertEquals("Login Test", user.name());
        assertEquals("login@example.com", user.email());
        assertEquals(Role.BORROWER, user.role());
        assertNull(user.passwordHash()); // Password should not be in returned user
    }

    // AUT-05: Login with wrong password fails with P21
    @Test
    void loginWrongPasswordThrowsP21() throws Exception {
        // Register first
        AuthService.register("Wrong Pass Test", "wrongpass@example.com", "correctpass", "123", "081");

        // Login with wrong password
        ValidationException ex = assertThrows(ValidationException.class, () ->
                AuthService.login("wrongpass@example.com", "wrongpassword")
        );

        assertEquals("p21", ex.getMessageKey());
    }

    // AUT-05: Login with non-existent email fails with P21 (same message as wrong password)
    @Test
    void loginNonExistentEmailThrowsP21() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                AuthService.login("nonexistent@example.com", "anypassword")
        );

        assertEquals("p21", ex.getMessageKey());
    }

    // AUT-05: Login with empty credentials fails with P21
    @Test
    void loginEmptyCredentialsThrowsP21() {
        ValidationException ex1 = assertThrows(ValidationException.class, () ->
                AuthService.login("", "password")
        );
        assertEquals("p21", ex1.getMessageKey());

        ValidationException ex2 = assertThrows(ValidationException.class, () ->
                AuthService.login("email@example.com", "")
        );
        assertEquals("p21", ex2.getMessageKey());
    }

    // Additional: Registered users are always borrowers
    @Test
    void registeredUsersAreBorrowers() throws Exception {
        User user = AuthService.register("Borrower", "borrower@example.com", "pass", "id", "phone");
        assertEquals(Role.BORROWER, user.role());
    }
}
