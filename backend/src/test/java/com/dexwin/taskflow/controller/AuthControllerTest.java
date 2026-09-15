package com.dexwin.taskflow.controller;

import com.dexwin.taskflow.entity.User;
import com.dexwin.taskflow.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(userRepository, authenticationManager);
    }

    @Test
    void loginCreatesSessionForValidCredentials() {
        User user = user("alice", "password123");
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "alice", null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, Object>> response = authController.login(
                Map.of("username", "alice", "password", "password123"),
                new MockHttpServletRequest());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(true, response.getBody().get("authenticated"));
        assertEquals("alice", response.getBody().get("username"));
    }

    @Test
    void loginRejectsInvalidCredentials() {
        doThrow(new BadCredentialsException("invalid"))
                .when(authenticationManager).authenticate(any());

        ResponseEntity<Map<String, Object>> response = authController.login(
                Map.of("username", "alice", "password", "wrong"),
                new MockHttpServletRequest());

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid username or password", response.getBody().get("message"));
    }

    @Test
    void loginRejectsMissingCredentials() {
        ResponseEntity<Map<String, Object>> response = authController.login(
                Map.of("username", "alice", "password", ""),
                new MockHttpServletRequest());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Username and password are required", response.getBody().get("message"));
    }

    @Test
    void logoutInvalidatesSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "alice", null, List.of());
        request.getSession();

        authController.logout(request, response, authentication);

        assertNull(request.getSession(false));
    }

    @Test
    void userPasswordIsNotSerialized() throws Exception {
        User user = user("alice", "password123");

        String json = new ObjectMapper().writeValueAsString(user);

        assertFalse(json.contains("password123"));
        assertFalse(json.contains("password"));
    }

    @Test
    void currentUserReturnsAuthenticatedUser() {
        User user = user("alice", "password123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "alice", null, List.of());

        Map<String, Object> currentUser = authController.currentUser(authentication);

        assertEquals(true, currentUser.get("authenticated"));
        assertEquals("alice", currentUser.get("username"));
        assertNotNull(currentUser.get("userId"));
        verify(userRepository).findByUsername("alice");
    }

    private User user(String username, String password) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@dexwin.test");
        user.setPassword(password);
        return user;
    }
}