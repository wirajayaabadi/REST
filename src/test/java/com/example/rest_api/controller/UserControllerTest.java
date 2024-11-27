package com.example.rest_api.controller;

import com.example.rest_api.entity.User;
import com.example.rest_api.model.request.RegisterUserRequest;
import com.example.rest_api.model.WebResponse;
import com.example.rest_api.model.response.UserResponse;
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
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void testRegisterUser() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setUsername("test");
    request.setName("Test");
    request.setPassword("test");

    mockMvc.perform(
            post("/api/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    ).andExpectAll(
            status().isOk()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
      });

      assertEquals("OK", response.getData());
    });
  }

  @Test
  void testRegisterBadRequest() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest();
    request.setUsername("");
    request.setName("");
    request.setPassword("");

    mockMvc.perform(
            post("/api/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    ).andExpectAll(
            status().isBadRequest()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
      });

      assertNotNull(response.getErrors());
    });
  }

  @Test
  void testRegisterDuplicate() throws Exception {
    User user = new User();
    user.setUsername("test");
    user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
    user.setName("Test");
    userRepository.save(user);

    RegisterUserRequest request = new RegisterUserRequest();
    request.setUsername("test");
    request.setName("Test");
    request.setPassword("test");

    mockMvc.perform(
            post("/api/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    ).andExpectAll(
            status().isBadRequest()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
      });

      assertNotNull(response.getErrors());
    });
  }

  @Test
  void getUserUnauthorizedTokenNotFound() throws Exception {
    mockMvc.perform(
            get("/api/users/current")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-API-TOKEN", "notfound")
    ).andExpectAll(
            status().isUnauthorized()
    ).andDo(result -> {
      WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
      });

      assertNotNull(response.getErrors());
    });
  }

  @Test
  void getUserSuccess() throws Exception {
    User user = new User();
    user.setUsername("test");
    user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
    user.setName("Test");
    user.setTokenExpiredAt(System.currentTimeMillis() + 10000);
    user.setToken("test");
    userRepository.save(user);



    mockMvc.perform(
            get("/api/users/current")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-API-TOKEN", "test")
    ).andExpectAll(
            status().isOk()
    ).andDo(result -> {
      WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
      });

      assertNull(response.getErrors());
      assertEquals("test", response.getData().getUsername());
      assertEquals("Test", response.getData().getName());
    });
  }
}