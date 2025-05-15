package com.blog.blogapi.controller;

import com.blog.blogapi.dto.LoginRequest;
import com.blog.blogapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.blog.blogapi.dto.RegisterRequest;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
           
            return jwtUtil.generateToken(loginRequest.getUsername());
        } catch (AuthenticationException e) {
            return "Kullanıcı adı veya şifre hatalı!";
        }
        
    }
    @Autowired
private UserRepository userRepository;

@Autowired
private PasswordEncoder passwordEncoder;

@PostMapping("/register")
public String register(@RequestBody RegisterRequest registerRequest) {
    if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
        return "Bu kullanıcı adı zaten alınmış!";
    }
    User user = new User();
    user.setUsername(registerRequest.getUsername());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setEmail(registerRequest.getEmail());
    userRepository.save(user);
    return "Kayıt başarılı!";
}
}