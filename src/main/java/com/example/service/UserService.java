package com.example.service;

import com.example.dto.request.ChangePasswordRequest;
import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse getCurrentUserProfile();

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void deactivateUser(Long userId);

    void activateUser(Long userId);

    List<UserResponse> getAllUsers();

    List<UserResponse> searchUsers(String keyword);

    void deleteUser(Long userId);
}
