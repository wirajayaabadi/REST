package com.example.rest_api.service;

import com.example.rest_api.entity.User;
import com.example.rest_api.model.request.LoginUserRequest;
import com.example.rest_api.model.response.TokenResponse;
import com.example.rest_api.repository.UserRepository;
import com.example.rest_api.security.BCrypt;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ValidationService validationService;

  @Transactional
  public TokenResponse login(LoginUserRequest request) {
    validationService.validate(request);

    User user = userRepository.findById(request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong"));

    if(BCrypt.checkpw(request.getPassword(), user.getPassword())) {
      user.setToken(UUID.randomUUID().toString());
      user.setTokenExpiredAt(next30Days());
      userRepository.save(user);

      return TokenResponse.builder().token(user.getToken()).expiredAt(user.getTokenExpiredAt()).build();
    } else {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password");
    }
  }

  private Long next30Days() {
    return System.currentTimeMillis() + (1000 * 60 * 24 * 30);
  }
}
