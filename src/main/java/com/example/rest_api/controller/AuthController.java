package com.example.rest_api.controller;

import com.example.rest_api.model.request.LoginUserRequest;
import com.example.rest_api.model.response.TokenResponse;
import com.example.rest_api.model.WebResponse;
import com.example.rest_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  @Autowired
  AuthService authService;

  @PostMapping(
          path = "/api/auth/login",
          consumes = MediaType.APPLICATION_JSON_VALUE,
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request) {
    TokenResponse tokenResponse = authService.login(request);

    return WebResponse.<TokenResponse>builder().data(tokenResponse).build();
  }
}
