package com.college.library.service;

import com.college.library.dto.LoginDto;
import com.college.library.dto.UserRegistrationDto;
import com.college.library.entity.Role;
import com.college.library.entity.User;
import com.college.library.exception.UnauthorizedException;
import com.college.library.exception.ValidationException;
import com.college.library.repository.UserRepository;
import com.college.library.util.PasswordUtil;
import com.college.library.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository = new UserRepository();

    public User loginStaff(LoginDto dto) {
        ValidationUtil.validate(dto);
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        if (user.getRole() == Role.MEMBER) {
            throw new UnauthorizedException("Students don't need an account — browse and download books freely!");
        }
        if (!PasswordUtil.verify(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        log.info("Staff logged in: {}", user.getUsername());
        return user;
    }

    public User login(LoginDto dto) {
        return loginStaff(dto);
    }

    public User register(UserRegistrationDto dto) {
        ValidationUtil.validate(dto);

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Email already registered");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.hash(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.MEMBER);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User createUser(UserRegistrationDto dto) {
        return register(dto);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(id);
    }
}
