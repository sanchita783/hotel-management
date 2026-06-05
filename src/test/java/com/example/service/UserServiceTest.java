package com.example.service;

import com.example.dto.request.ChangePasswordRequest;
import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.UserResponse;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.DuplicateResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedAccessException;
import com.example.repository.UserRepository;
import com.example.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phone("9876543210")
                .role(Role.CUSTOMER)
                .isActive(true)
                .emailVerified(true)
                .build();

        adminUser = User.builder()
                .id(99L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@hotel.com")
                .password("encodedPassword")
                .phone("9000000001")
                .role(Role.ADMIN)
                .isActive(true)
                .emailVerified(true)
                .build();

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById - returns user when found")
    void getUserById_ShouldReturnUser_WhenExists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("getUserById - throws ResourceNotFoundException when not found")
    void getUserById_ShouldThrow_WhenNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ─── getCurrentUserProfile ─────────────────────────────────────────────────

    @Test
    @DisplayName("getCurrentUserProfile - returns profile of logged-in user")
    void getCurrentUserProfile_ShouldReturnCurrentUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        UserResponse response = userService.getCurrentUserProfile();

        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    // ─── updateProfile ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile - updates own profile successfully")
    void updateProfile_ShouldUpdate_WhenOwnProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane", "Doe", "9123456789", "New Address");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByPhone("9123456789")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        UserResponse response = userService.updateProfile(1L, request);

        assertThat(testUser.getFirstName()).isEqualTo("Jane");
        assertThat(testUser.getPhone()).isEqualTo("9123456789");
        then(userRepository).should().save(testUser);
    }

    @Test
    @DisplayName("updateProfile - throws UnauthorizedAccessException when updating another user")
    void updateProfile_ShouldThrow_WhenUpdatingAnotherUser() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane", "Doe", null, null);

        assertThatThrownBy(() -> userService.updateProfile(2L, request))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("own profile");
    }

    @Test
    @DisplayName("updateProfile - throws DuplicateResourceException when phone taken")
    void updateProfile_ShouldThrow_WhenPhoneAlreadyTaken() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "John", "Doe", "9999999999", null);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByPhone("9999999999")).willReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phone");
    }

    // ─── changePassword ────────────────────────────────────────────────────────

    @Test
    @DisplayName("changePassword - changes password successfully")
    void changePassword_ShouldSucceed_WhenCurrentPasswordCorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@1", "NewPass@1", "NewPass@1");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("OldPass@1", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("NewPass@1")).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        assertThatNoException().isThrownBy(() -> userService.changePassword(1L, request));
        assertThat(testUser.getPassword()).isEqualTo("newEncodedPassword");
    }

    @Test
    @DisplayName("changePassword - throws when current password is wrong")
    void changePassword_ShouldThrow_WhenCurrentPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPass@1", "NewPass@1", "NewPass@1");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("WrongPass@1", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    @DisplayName("changePassword - throws when new passwords do not match")
    void changePassword_ShouldThrow_WhenPasswordsDoNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@1", "NewPass@1", "DifferentPass@1");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("OldPass@1", "encodedPassword")).willReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    @DisplayName("changePassword - throws when changing another user's password")
    void changePassword_ShouldThrow_WhenChangingAnotherUsersPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPass@1", "NewPass@1", "NewPass@1");

        assertThatThrownBy(() -> userService.changePassword(2L, request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    // ─── deactivate/activate ───────────────────────────────────────────────────

    @Test
    @DisplayName("deactivateUser - sets isActive to false")
    void deactivateUser_ShouldDeactivate() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willReturn(testUser);

        userService.deactivateUser(1L);

        assertThat(testUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("activateUser - sets isActive to true")
    void activateUser_ShouldActivate() {
        testUser.setIsActive(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willReturn(testUser);

        userService.activateUser(1L);

        assertThat(testUser.getIsActive()).isTrue();
    }

    // ─── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers - returns all users")
    void getAllUsers_ShouldReturnAll() {
        given(userRepository.findAll()).willReturn(List.of(testUser, adminUser));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
    }

    // ─── searchUsers ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchUsers - returns matching users")
    void searchUsers_ShouldReturnMatches() {
        given(userRepository.searchUsers("john")).willReturn(List.of(testUser));

        List<UserResponse> results = userService.searchUsers("john");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("john@example.com");
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser - deletes user")
    void deleteUser_ShouldDelete() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        willDoNothing().given(userRepository).delete(testUser);

        assertThatNoException().isThrownBy(() -> userService.deleteUser(1L));
        then(userRepository).should().delete(testUser);
    }

    @Test
    @DisplayName("deleteUser - throws when user not found")
    void deleteUser_ShouldThrow_WhenNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
