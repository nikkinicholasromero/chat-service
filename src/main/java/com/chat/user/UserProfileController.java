package com.chat.user;

import com.chat.user.dto.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
public class UserProfileController {
    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public EmailStatusResponse getEmailStatus(@RequestParam String email) {
        log.info("getEmailStatus {}", email);
        return new EmailStatusResponse(userProfileService.getEmailStatus(email));
    }

    @PostMapping("/registration")
    public void registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("registerUser {}", request);
        userProfileService.registerUser(request);
    }

    @PostMapping("/registration/confirmation/{email}")
    public void sendConfirmationCode(@PathVariable String email) {
        log.info("sendConfirmationCode {}", email);
        userProfileService.sendConfirmationCode(email);
    }

    @PostMapping("/registration/confirmation")
    public void confirmRegistration(@Valid @RequestBody ConfirmationRegistrationRequest request) {
        log.info("confirmRegistration {}", request);
        userProfileService.confirmRegistration(request);
    }

    @PostMapping("/password/reset/{email}")
    public void sendPasswordResetCode(@PathVariable String email) {
        log.info("sendPasswordResetCode {}", email);
        userProfileService.sendPasswordResetCode(email);
    }

    @PostMapping("/password/reset")
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("resetPassword {}", request);
        userProfileService.resetPassword(request);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public GetUserProfileResponse getUserProfile() {
        log.info("getUserProfile");
        return userProfileService.getUserProfile();
    }

    @PostMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public void updateUserProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        log.info("updateUserProfile {}", request);
        userProfileService.updateUserProfile(request);
    }

    @PostMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("updatePassword {}", request);
        userProfileService.updatePassword(request);
    }
}
