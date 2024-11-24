package com.example.rest_api.service;

import com.example.rest_api.entity.User;
import com.example.rest_api.model.RegisterUserRequest;
import com.example.rest_api.repository.UserRepository;
import com.example.rest_api.security.BCrypt;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Validator validator;

  @Transactional
  public void register(RegisterUserRequest request) {
    Set<ConstraintViolation<RegisterUserRequest>> constraintViolationSet = validator.validate(request);

    if(constraintViolationSet.size() != 0) {
      throw new ConstraintViolationException(constraintViolationSet);
    }

    if(userRepository.existsById(request.getUsername())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
    user.setName(request.getName());

    userRepository.save(user);
  }
}
