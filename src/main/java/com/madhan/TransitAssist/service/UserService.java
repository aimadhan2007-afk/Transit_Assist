package com.madhan.TransitAssist.service;

import com.madhan.TransitAssist.dto.RegisterRequest;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.exception.ResourceNotFoundException;
import com.madhan.TransitAssist.model.Role;
import com.madhan.TransitAssist.model.User;
import com.madhan.TransitAssist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request, boolean isPublic) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessRuleException("An account with email " + request.getEmail() + " already exists.");
        }

        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("Password and Confirm Password do not match.");
        }

        Role assignedRole = Role.USER;
        if (!isPublic && request.getRole() != null) {
            assignedRole = request.getRole();
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone().trim());
        user.setRole(assignedRole);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User toggleUserStatus(Long id) {
        User user = findById(id);
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessRuleException("Administrator account status cannot be modified.");
        }
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public User createStaff(RegisterRequest request) {
        request.setRole(Role.STAFF);
        return registerUser(request, false);
    }

    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Transactional(readOnly = true)
    public long countTotalUsers() {
        return userRepository.count();
    }
}
