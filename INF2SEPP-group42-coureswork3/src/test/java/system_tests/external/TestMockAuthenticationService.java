package system_tests.external;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import external.MockAuthenticationService;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class TestMockAuthenticationService {
    private MockAuthenticationService auth;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException, ParseException {
        auth = new MockAuthenticationService();
    }

    @Test
    @DisplayName("Standard login with correct admin1 credentials returns user JSON")
    public void testLogin_ValidAdminCredentials_ReturnsUserJson() {
        String result = auth.login("admin1", "admin1pass");
        assertTrue(result.contains("admin1@hindeburg.ac.uk"),
            "Expected login to return user JSON with correct email for valid credentials.");
    }

    @Test
    @DisplayName("Login with correct username and wrong password returns error JSON")
    public void testLogin_CorrectUsernameWrongPassword_ReturnsError() {
        String result = auth.login("admin1", "wrongpass");
        JSONObject expected = new JSONObject();
        expected.put("error", "Wrong username or password");
        assertEquals(expected.toJSONString(), result,
            "Expected error JSON when password is incorrect.");
    }

    @Test
    @DisplayName("Login with wrong username and correct password returns error JSON")
    public void testLogin_WrongUsernameCorrectPassword_ReturnsError() {
        String result = auth.login("wronguser", "admin1pass");
        JSONObject expected = new JSONObject();
        expected.put("error", "Wrong username or password");
        assertEquals(expected.toJSONString(), result,
            "Expected error JSON when username is not registered.");
    }

    @Test
    @DisplayName("Login with non-existent username returns error JSON")
    public void testLogin_UsernameDoesNotExist_ReturnsError() {
        String result = auth.login("notAUser", "somepass");
        JSONObject expected = new JSONObject();
        expected.put("error", "Wrong username or password");
        assertEquals(expected.toJSONString(), result,
            "Expected error JSON when login is attempted with a completely unknown user.");
    }
}
