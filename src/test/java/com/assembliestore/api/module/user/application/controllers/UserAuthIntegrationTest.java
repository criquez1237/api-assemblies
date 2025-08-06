package com.assembliestore.api.module.user.application.controllers;

import com.assembliestore.api.module.user.infrastructure.adapter.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class UserAuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignupAndSignin() throws Exception {
        // Signup
        SignUpRequest signup = new SignUpRequest();
        signup.setEmail("testuser@example.com");
        signup.setPassword("TestPass123");
        signup.setNames("Test");
        signup.setSurnames("User");
        signup.setImagePerfil("img.png");
        signup.setPhone("1234567890");

        ResultActions signupResult = mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)));
        signupResult.andExpect(status().isCreated());

        // Signin
        SignInRequest login = new SignInRequest();
        login.setEmail("testuser@example.com");
        login.setPassword("TestPass123");

        ResultActions signinResult = mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)));
        signinResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.userName").value("testuser"));
    }
}
