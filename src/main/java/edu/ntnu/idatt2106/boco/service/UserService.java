package edu.ntnu.idatt2106.boco.service;

import edu.ntnu.idatt2106.boco.models.User;
import edu.ntnu.idatt2106.boco.payload.request.RegisterUserRequest;
import edu.ntnu.idatt2106.boco.payload.response.LoginResponse;
import edu.ntnu.idatt2106.boco.payload.response.MessageResponse;
import edu.ntnu.idatt2106.boco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public boolean userExists(String email)
    {
        return userRepository.existsByEmail(email);
    }

    public User register(RegisterUserRequest request)
    {
        User user = new User(
                request.getName(),
                request.isPerson(),
                request.getAddress(),
                request.getEmail(),
                encoder.encode(request.getPassword()),
                "USER"
        );

        userRepository.save(user);

        return user;
    }
}
