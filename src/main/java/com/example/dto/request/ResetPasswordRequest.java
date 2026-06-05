package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * DTO for the /auth/reset-password endpoint.
 *
 * The reset flow is:
 *   1. User clicks "Forgot Password" → receives a one-time token by email.
 *   2. User opens the reset link (carries the token) and enters a NEW password
 *      + CONFIRM password.  No current/old password is needed – the token
 *      already proves identity.
 *
 * Fields:
 *   - token          : the UUID reset token from the email link (required)
 *   - newPassword    : the desired new password (validated for strength)
 *   - confirmPassword: must match newPassword (cross-field check in service)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    /**
     * One-time password-reset token sent to the user's email.
     * Stored in User.passwordResetToken and expires after 1 hour.
     */
    @NotBlank(message = "Reset token is required")
    private String token;

    /**
     * The new password the user wants to set.
     * Must be at least 8 characters and contain:
     *   - one uppercase letter
     *   - one lowercase letter
     *   - one digit
     *   - one special character (@$!%*?&)
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, " +
                  "one digit and one special character (@$!%*?&)"
    )
    private String newPassword;

    /**
     * Must be identical to newPassword.
     * Cross-field validation is performed in AuthServiceImpl.resetPassword().
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}