package com.example.rest_api.service;

import com.example.rest_api.entity.User;
import com.example.rest_api.model.request.RegisterUserRequest;
import com.example.rest_api.model.response.UserResponse;
import com.example.rest_api.repository.UserRepository;
import com.example.rest_api.security.BCrypt;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ValidationService validationService;

  @Transactional
  public void register(RegisterUserRequest request) {
    validationService.validate(request);

    if(userRepository.existsById(request.getUsername())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
    user.setName(request.getName());

    userRepository.save(user);
  }

  public UserResponse get(User user) {
    return UserResponse.builder()
            .username(user.getUsername())
            .name(user.getName())
            .build();
  }
}
