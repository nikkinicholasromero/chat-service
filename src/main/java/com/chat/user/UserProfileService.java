package com.chat.user;

import com.chat.common.encryption.HashService;
import com.chat.common.exception.FailedPreconditionException;
import com.chat.common.mail.Mail;
import com.chat.common.mail.MailService;
import com.chat.common.mail.MailTemplate;
import com.chat.common.security.SecurityContextHelper;
import com.chat.token.dto.SocialProfile;
import com.chat.user.dto.*;
import com.chat.user.repository.UserProfile;
import com.chat.user.repository.UserProfileId;
import com.chat.user.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final HashService hashService;
    private final MailService mailService;
    private final SecurityContextHelper securityContextHelper;

    public UserProfileService(
            UserProfileRepository userProfileRepository,
            HashService hashService,
            MailService mailService,
            SecurityContextHelper securityContextHelper) {
        this.userProfileRepository = userProfileRepository;
        this.hashService = hashService;
        this.mailService = mailService;
        this.securityContextHelper = securityContextHelper;
    }

    public EmailStatus getEmailStatus(String email) {
        Optional<UserProfile> userProfile = userProfileRepository.findByEmail(cleanEmail(email));

        if (userProfile.isEmpty()) {
            return EmailStatus.UNREGISTERED;
        }

        if (userProfile.get().confirmed()) {
            return EmailStatus.CONFIRMED;
        }

        return EmailStatus.UNCONFIRMED;
    }

    @Transactional
    public void registerUser(RegistrationRequest request) {
        if (request.social()) {
            UserProfile userProfile = UserProfile.social(
                    new UserProfileId(UUID.randomUUID().toString()),
                    cleanEmail(request.email()),
                    StringUtils.trimToEmpty(request.firstName()),
                    StringUtils.trimToEmpty(request.lastName()));

            userProfileRepository.save(userProfile);
        } else if (!EmailStatus.UNREGISTERED.equals(getEmailStatus(request.email()))) {
            throw new FailedPreconditionException("errors.email.registered");
        } else {
            String salt = hashService.generateRandomSalt();
            String hash = hashService.hash(request.password(), salt);
            String confirmationCode = UUID.randomUUID().toString();

            UserProfile userProfile = UserProfile.regular(
                    new UserProfileId(UUID.randomUUID().toString()),
                    cleanEmail(request.email()),
                    salt,
                    hash,
                    confirmationCode,
                    StringUtils.trimToEmpty(request.firstName()),
                    StringUtils.trimToEmpty(request.lastName()));

            userProfileRepository.save(userProfile);

            sendConfirmationCode(
                    userProfile.firstName(),
                    userProfile.lastName(),
                    userProfile.email(),
                    userProfile.confirmationCode());
        }
    }

    @Transactional
    public void registerSocialUser(SocialProfile socialProfile) {
        String email = cleanEmail(socialProfile.email());
        EmailStatus emailStatus = getEmailStatus(email);
        if (EmailStatus.UNREGISTERED.equals(emailStatus)) {
            registerUser(
                    new RegistrationRequest(
                            email,
                            null,
                            socialProfile.firstName(),
                            socialProfile.lastName(),
                            true));
        } else if (EmailStatus.UNCONFIRMED.equals(emailStatus)) {
            confirmRegistration(
                    new ConfirmationRegistrationRequest(
                            email,
                            null,
                            true));
        }
    }

    public void sendConfirmationCode(String email) {
        EmailStatus emailStatus = getEmailStatus(email);

        if (EmailStatus.UNREGISTERED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.unregistered");
        }

        if (EmailStatus.CONFIRMED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.alreadyConfirmed");
        }

        UserProfile userProfile = getUserProfile(email);

        sendConfirmationCode(
                userProfile.firstName(),
                userProfile.lastName(),
                userProfile.email(),
                userProfile.confirmationCode());
    }

    @Transactional
    public void confirmRegistration(ConfirmationRegistrationRequest request) {
        if (request.social()) {
            UserProfile userProfile = getUserProfile(request.email());

            userProfile.confirmRegistration();
            userProfileRepository.save(userProfile);
            return;
        }

        EmailStatus emailStatus = getEmailStatus(request.email());

        if (EmailStatus.UNREGISTERED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.unregistered");
        }

        if (EmailStatus.CONFIRMED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.alreadyConfirmed");
        }

        UserProfile userProfile = getUserProfile(request.email());

        if (!request.confirmationCode().equals(userProfile.confirmationCode())) {
            throw new FailedPreconditionException("errors.invalidConfirmationCode");
        }

        userProfile.confirmRegistration();
        userProfileRepository.save(userProfile);
    }

    @Transactional
    public void sendPasswordResetCode(String email) {
        if (EmailStatus.UNREGISTERED.equals(getEmailStatus(email))) {
            return;
        }

        UserProfile userProfile = getUserProfile(email);

        String passwordResetCode = UUID.randomUUID().toString();
        userProfile.setPasswordResetCode(passwordResetCode);
        userProfileRepository.save(userProfile);

        sendPasswordResetCode(
                userProfile.firstName(),
                userProfile.lastName(),
                userProfile.email(),
                passwordResetCode);
    }

    public void resetPassword(PasswordResetRequest request) {
        if (EmailStatus.UNREGISTERED.equals(getEmailStatus(request.email()))) {
            throw new FailedPreconditionException("errors.email.unregistered");
        }

        UserProfile userProfile = getUserProfile(request.email());

        if (!request.passwordResetCode().equals(userProfile.passwordResetCode())) {
            throw new FailedPreconditionException("errors.invalidPasswordResetCode");
        }

        String salt = hashService.generateRandomSalt();
        String hash = hashService.hash(request.newPassword(), salt);
        userProfile.updatePassword(salt, hash);
        userProfileRepository.save(userProfile);
    }

    public GetUserProfileResponse getUserProfile() {
        UserProfile userProfile = getUserProfile(securityContextHelper.principal().email());

        return new GetUserProfileResponse(
                userProfile.firstName(),
                userProfile.lastName(),
                Objects.isNull(userProfile.hash()));
    }

    public void updateUserProfile(UpdateUserProfileRequest request) {
        UserProfile userProfile = getUserProfile(securityContextHelper.principal().email());

        userProfile.updateProfile(
                StringUtils.trimToEmpty(request.firstName()),
                StringUtils.trimToEmpty(request.lastName()));

        userProfileRepository.save(userProfile);
    }

    public void updatePassword(UpdatePasswordRequest request) {
        String email = securityContextHelper.principal().email();
        validateEmail(email);
        UserProfile userProfile = getUserProfile(email);
        if (Objects.nonNull(userProfile.hash())) {
            validatePassword(userProfile, request.currentPassword());
        }

        String salt = hashService.generateRandomSalt();
        String hash = hashService.hash(request.newPassword(), salt);
        userProfile.updatePassword(salt, hash);
        userProfileRepository.save(userProfile);
    }

    public void validateCredentials(String email, String password) {
        validateEmail(email);
        UserProfile userProfile = getUserProfile(email);
        validatePassword(userProfile, password);
    }

    private void validateEmail(String email) {
        EmailStatus emailStatus = getEmailStatus(email);

        if (EmailStatus.UNREGISTERED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.unregistered");
        }

        if (EmailStatus.UNCONFIRMED.equals(emailStatus)) {
            throw new FailedPreconditionException("errors.email.unconfirmed");
        }
    }

    private void validatePassword(UserProfile userProfile, String password) {
        if (!hashService.hash(password, userProfile.salt()).equals(userProfile.hash())) {
            throw new FailedPreconditionException("errors.credentials.incorrect");
        }
    }

    private String cleanEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase();
    }

    public UserProfile getUserProfile(String email) {
        return userProfileRepository.findByEmail(cleanEmail(email)).orElseThrow();
    }

    private void sendConfirmationCode(String firstName, String lastName, String email, String confirmationCode) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("name", firstName + " " + lastName);
        variables.put("confirmation_code", confirmationCode);
        Mail mail = new Mail(MailTemplate.REGISTRATION_CONFIRMATION, Collections.singletonList(email), variables);
        mailService.send(mail);
    }

    private void sendPasswordResetCode(String firstName, String lastName, String email, String passwordResetCode) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("email", email);
        variables.put("name", firstName + " " + lastName);
        variables.put("password_reset_code", passwordResetCode);
        Mail mail = new Mail(MailTemplate.PASSWORD_RESET, Collections.singletonList(email), variables);
        mailService.send(mail);
    }
}
