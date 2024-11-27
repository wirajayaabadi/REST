package com.example.rest_api.controller;

import com.example.rest_api.entity.User;
import com.example.rest_api.model.request.LoginUserRequest;
import com.example.rest_api.model.response.TokenResponse;
import com.example.rest_api.model.WebResponse;
import com.example.rest_api.repository.UserRepository;
import com.example.rest_api.security.BCrypt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void loginFailedUserNotFound() throws Exception{
    LoginUserRequest loginUserRequest = new LoginUserRequest();
    loginUserRequest.setUsername("test");
    loginUserRequest.setPassword("test");

    mockMvc.perform(
            post("/api/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginUserRequest))
    ).andExpectAll(
            status().isUnauthorized()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
      });

      assertNotNull(response.getErrors());
    });
  }

  @Test
  void loginFailedWrongPassword() throws Exception{
    User user = new User();
    user.setName("Test");
    user.setUsername("test");
    user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
    userRepository.save(user);

    LoginUserRequest request = new LoginUserRequest();
    request.setUsername("test");
    request.setPassword("salah");

    mockMvc.perform(
            post("/api/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    ).andExpectAll(
            status().isUnauthorized()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
      });

      assertNotNull(response.getErrors());
    });
  }

  @Test
  void loginSucces() throws Exception{
    User user = new User();
    user.setName("Test");
    user.setUsername("test");
    user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
    userRepository.save(user);

    LoginUserRequest request = new LoginUserRequest();
    request.setUsername("test");
    request.setPassword("test");

    mockMvc.perform(
            post("/api/auth/login")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    ).andExpectAll(
            status().isOk()
    ).andDo(result -> {
      WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
      });

      assertNull(response.getErrors());
      assertNotNull(response.getData().getToken());
      assertNotNull(response.getData().getExpiredAt());

      User userDb = userRepository.findById("test").orElse(null);
      assertNotNull(userDb);
      assertEquals(userDb.getToken(), response.getData().getToken());
      assertEquals(userDb.getTokenExpiredAt(), response.getData().getExpiredAt());
    });
  }
}