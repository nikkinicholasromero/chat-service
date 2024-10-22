package com.chat.user;

import com.chat.BaseUnitTest;
import com.chat.common.encryption.HashService;
import com.chat.common.exception.FailedPreconditionException;
import com.chat.common.mail.Mail;
import com.chat.common.mail.MailService;
import com.chat.common.mail.MailTemplate;
import com.chat.common.security.SecurityContextHelper;
import com.chat.common.security.UserPrincipal;
import com.chat.token.dto.SocialProfile;
import com.chat.user.dto.EmailStatus;
import com.chat.user.dto.*;
import com.chat.common.repository.UserProfile;
import com.chat.common.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserProfileServiceTest extends BaseUnitTest {
    @InjectMocks
    private UserProfileService target;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private HashService hashService;

    @Mock
    private MailService mailService;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @Mock
    private UserProfile userProfile;

    @Captor
    private ArgumentCaptor<UserProfile> userProfileCaptor;

    @Captor
    private ArgumentCaptor<Mail> mailCaptor;

    @Captor
    private ArgumentCaptor<String> passwordCaptor;

    @Captor
    private ArgumentCaptor<String> saltCaptor;

    private String email;

    @BeforeEach
    public void setup() {
        email = " NIKKI@gmail.com ";

        when(userProfile.email()).thenReturn("nikki@gmail.com");
        when(userProfile.firstName()).thenReturn("Nikki Nicholas");
        when(userProfile.lastName()).thenReturn("Romero");
        when(userProfile.confirmationCode()).thenReturn("some confirmation code");
    }

    @Test
    void getEmailStatus_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        EmailStatus actual = target.getEmailStatus(email);
        assertThat(actual).isEqualTo(EmailStatus.UNREGISTERED);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
    }

    @Test
    void getEmailStatus_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        EmailStatus actual = target.getEmailStatus(email);
        assertThat(actual).isEqualTo(EmailStatus.CONFIRMED);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
    }

    @Test
    void getEmailStatus_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);

        EmailStatus actual = target.getEmailStatus(email);
        assertThat(actual).isEqualTo(EmailStatus.UNCONFIRMED);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
    }

    @Test
    void registerUser_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        RegistrationRequest request = new RegistrationRequest(
                email,
                "some password",
                " Nikki Nicholas ",
                " Romero ",
                false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.registerUser(request));
        assertThat(e.getMessage()).isEqualTo("errors.email.registered");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void registerUser_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);

        RegistrationRequest request = new RegistrationRequest(
                email,
                "some password",
                " Nikki Nicholas ",
                " Romero ",
                false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.registerUser(request));
        assertThat(e.getMessage()).isEqualTo("errors.email.registered");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void registerUser_regular() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(hashService.generateRandomSalt()).thenReturn("some salt");
        when(hashService.hash(any(), any())).thenReturn("some hash");

        RegistrationRequest request = new RegistrationRequest(
                email,
                "some password",
                " Nikki Nicholas ",
                " Romero ",
                false);

        target.registerUser(request);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(hashService).generateRandomSalt();
        verify(hashService).hash("some password", "some salt");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        assertThat(actual.id()).isNotBlank();
        assertThat(actual.email()).isEqualTo("nikki@gmail.com");
        assertThat(actual.salt()).isEqualTo("some salt");
        assertThat(actual.hash()).isEqualTo("some hash");
        assertThat(actual.confirmed()).isFalse();
        assertThat(actual.confirmationCode()).isNotBlank();
        assertThat(actual.firstName()).isEqualTo("Nikki Nicholas");
        assertThat(actual.lastName()).isEqualTo("Romero");
        verify(mailService).send(mailCaptor.capture());
        Mail mail = mailCaptor.getValue();
        assertEquals(MailTemplate.REGISTRATION_CONFIRMATION, mail.template());
        assertEquals(List.of("nikki@gmail.com"), mail.recipients());
        assertThat(mail.variables()).containsEntry("email", "nikki@gmail.com");
        assertThat(mail.variables()).containsEntry("name", "Nikki Nicholas Romero");
        assertThat(mail.variables().get("confirmation_code")).isNotNull();
    }

    @Test
    void registerUser_social() {
        RegistrationRequest request = new RegistrationRequest(
                email,
                "some password",
                " Nikki Nicholas ",
                " Romero ",
                true);

        target.registerUser(request);

        verify(userProfileRepository, never()).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        assertThat(actual.id()).isNotBlank();
        assertThat(actual.email()).isEqualTo("nikki@gmail.com");
        assertNull(actual.salt());
        assertNull(actual.hash());
        assertTrue(actual.confirmed());
        assertNull(actual.confirmationCode());
        assertThat(actual.firstName()).isEqualTo("Nikki Nicholas");
        assertThat(actual.lastName()).isEqualTo("Romero");

        verifyNoInteractions(mailService);
        verifyNoInteractions(hashService);
    }

    @Test
    void registerSocialUser_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        SocialProfile socialProfile = new SocialProfile(
                " NIKKI@gmail.com ",
                "Nikki Nicholas",
                "Romero");

        target.registerSocialUser(socialProfile);

        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        assertThat(actual.id()).isNotBlank();
        assertThat(actual.email()).isEqualTo("nikki@gmail.com");
        assertNull(actual.salt());
        assertNull(actual.hash());
        assertTrue(actual.confirmed());
        assertNull(actual.confirmationCode());
        assertThat(actual.firstName()).isEqualTo("Nikki Nicholas");
        assertThat(actual.lastName()).isEqualTo("Romero");

        verifyNoInteractions(mailService);
        verifyNoInteractions(hashService);
    }

    @Test
    void registerSocialUser_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));

        SocialProfile socialProfile = new SocialProfile(
                " NIKKI@gmail.com ",
                "Nikki Nicholas",
                "Romero");

        target.registerSocialUser(socialProfile);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).confirmRegistration();
    }

    @Test
    void registerSocialUser() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        SocialProfile socialProfile = new SocialProfile(
                " NIKKI@gmail.com ",
                "Nikki Nicholas",
                "Romero");

        target.registerSocialUser(socialProfile);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void sendConfirmationCode_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.sendConfirmationCode(email));
        assertThat(e.getMessage()).isEqualTo("errors.email.unregistered");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void sendConfirmationCode_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.sendConfirmationCode(email));
        assertThat(e.getMessage()).isEqualTo("errors.email.alreadyConfirmed");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void sendConfirmationCode_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);

        target.sendConfirmationCode(email);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(mailService).send(mailCaptor.capture());
        Mail mail = mailCaptor.getValue();
        assertEquals(MailTemplate.REGISTRATION_CONFIRMATION, mail.template());
        assertEquals(List.of("nikki@gmail.com"), mail.recipients());
        assertThat(mail.variables()).containsEntry("email", "nikki@gmail.com");
        assertThat(mail.variables()).containsEntry("name", "Nikki Nicholas Romero");
        assertThat(mail.variables().get("confirmation_code")).isNotNull();
    }

    @Test
    void confirmRegistration_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                email,
                "some confirmation code",
                false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.confirmRegistration(request));
        assertThat(e.getMessage()).isEqualTo("errors.email.unregistered");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void confirmRegistration_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                email,
                "some confirmation code",
                false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.confirmRegistration(request));
        assertThat(e.getMessage()).isEqualTo("errors.email.alreadyConfirmed");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void confirmRegistration_invalidConfirmationCode() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);
        when(userProfile.confirmationCode()).thenReturn("some confirmation code");

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                email,
                "some invalid confirmation code",
                false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.confirmRegistration(request));
        assertThat(e.getMessage()).isEqualTo("errors.invalidConfirmationCode");

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void confirmRegistration_regular() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);
        when(userProfile.confirmationCode()).thenReturn("some confirmation code");

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                email,
                "some confirmation code",
                false);

        target.confirmRegistration(request);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).confirmRegistration();
    }

    @Test
    void confirmRegistration_social() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));

        ConfirmationRegistrationRequest request = new ConfirmationRegistrationRequest(
                email,
                "some confirmation code",
                true);

        target.confirmRegistration(request);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).confirmRegistration();
    }

    @Test
    void sendPasswordResetCode_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        target.sendPasswordResetCode(email);

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void sendPasswordResetCode_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);

        target.sendPasswordResetCode(email);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).setPasswordResetCode(anyString());
        verify(mailService).send(mailCaptor.capture());
        Mail mail = mailCaptor.getValue();
        assertEquals(MailTemplate.PASSWORD_RESET, mail.template());
        assertEquals(List.of("nikki@gmail.com"), mail.recipients());
        assertThat(mail.variables()).containsEntry("email", "nikki@gmail.com");
        assertThat(mail.variables()).containsEntry("name", "Nikki Nicholas Romero");
        assertThat(mail.variables().get("password_reset_code")).isNotNull();
    }

    @Test
    void sendPasswordResetCode_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);

        target.sendPasswordResetCode(email);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).setPasswordResetCode(anyString());
        verify(mailService).send(mailCaptor.capture());
        Mail mail = mailCaptor.getValue();
        assertEquals(MailTemplate.PASSWORD_RESET, mail.template());
        assertEquals(List.of("nikki@gmail.com"), mail.recipients());
        assertThat(mail.variables()).containsEntry("email", "nikki@gmail.com");
        assertThat(mail.variables()).containsEntry("name", "Nikki Nicholas Romero");
        assertThat(mail.variables().get("password_reset_code")).isNotNull();
    }

    @Test
    void resetPassword_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        PasswordResetRequest request = new PasswordResetRequest(
                email,
                "some password reset code",
                "some new password");

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo("errors.email.unregistered");

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(hashService);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void resetPassword_unconfirmed_invalidPasswordResetCode() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);
        when(userProfile.passwordResetCode()).thenReturn("some password reset code");

        PasswordResetRequest request = new PasswordResetRequest(
                email,
                "some invalid password reset code",
                "some new password");

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo("errors.invalidPasswordResetCode");

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verifyNoInteractions(hashService);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void resetPassword_confirmed_invalidPasswordResetCode() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.passwordResetCode()).thenReturn("some password reset code");

        PasswordResetRequest request = new PasswordResetRequest(
                email,
                "some invalid password reset code",
                "some new password");

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.resetPassword(request));
        assertThat(e.getMessage()).isEqualTo("errors.invalidPasswordResetCode");

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verifyNoInteractions(hashService);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void resetPassword_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);
        when(userProfile.passwordResetCode()).thenReturn("some password reset code");
        when(hashService.generateRandomSalt()).thenReturn("some salt");
        when(hashService.hash(any(), any())).thenReturn("some hash");

        PasswordResetRequest request = new PasswordResetRequest(
                email,
                "some password reset code",
                "some new password");

        target.resetPassword(request);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService).generateRandomSalt();
        verify(hashService).hash("some new password", "some salt");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).updatePassword("some salt", "some hash");
    }

    @Test
    void resetPassword_confirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.passwordResetCode()).thenReturn("some password reset code");
        when(hashService.generateRandomSalt()).thenReturn("some salt");
        when(hashService.hash(any(), any())).thenReturn("some hash");

        PasswordResetRequest request = new PasswordResetRequest(
                email,
                "some password reset code",
                "some new password");

        target.resetPassword(request);

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService).generateRandomSalt();
        verify(hashService).hash("some new password", "some salt");
        verify(userProfileRepository).save(userProfileCaptor.capture());
        UserProfile actual = userProfileCaptor.getValue();
        verify(actual).updatePassword("some salt", "some hash");
    }

    @Test
    void getUserProfile() {
        when(securityContextHelper.principal()).thenReturn(new UserPrincipal("nikki@gmail.com", "some token"));
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));

        GetUserProfileResponse actual = target.getUserProfile();
        assertEquals("Nikki Nicholas", actual.firstName());
        assertEquals("Romero", actual.lastName());

        verify(securityContextHelper).principal();
        verify(userProfileRepository).findByEmail("nikki@gmail.com");
    }

    @Test
    void updateUserProfile() {
        when(securityContextHelper.principal()).thenReturn(new UserPrincipal("nikki@gmail.com", "some token"));
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                " New First Name ",
                " New Last Name ");

        target.updateUserProfile(request);

        verify(securityContextHelper).principal();
        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verify(userProfile).updateProfile("New First Name", "New Last Name");
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void updatePassword_noPasswordYet() {
        when(securityContextHelper.principal()).thenReturn(new UserPrincipal("nikki@gmail.com", "some token"));
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.salt()).thenReturn(null);
        when(userProfile.hash()).thenReturn(null);
        when(hashService.hash(any(), any())).thenReturn("some new hash");
        when(hashService.generateRandomSalt()).thenReturn("some new salt");

        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                "some new password");

        target.updatePassword(request);

        verify(securityContextHelper, times(1)).principal();
        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService, times(1)).hash(passwordCaptor.capture(), saltCaptor.capture());
        assertEquals(List.of("some new password"), passwordCaptor.getAllValues());
        assertEquals(List.of("some new salt"), saltCaptor.getAllValues());
        verify(hashService).generateRandomSalt();
        verify(userProfile).updatePassword("some new salt", "some new hash");
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void updatePassword() {
        when(securityContextHelper.principal()).thenReturn(new UserPrincipal("nikki@gmail.com", "some token"));
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.salt()).thenReturn("some salt");
        when(userProfile.hash()).thenReturn("some hash");
        when(hashService.hash(any(), any())).thenReturn("some hash", "some new hash");
        when(hashService.generateRandomSalt()).thenReturn("some new salt");

        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "some current password",
                "some new password");

        target.updatePassword(request);

        verify(securityContextHelper, times(1)).principal();
        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService, times(2)).hash(passwordCaptor.capture(), saltCaptor.capture());
        assertEquals(List.of("some current password", "some new password"), passwordCaptor.getAllValues());
        assertEquals(List.of("some salt", "some new salt"), saltCaptor.getAllValues());
        verify(hashService).generateRandomSalt();
        verify(userProfile).updatePassword("some new salt", "some new hash");
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void validateCredentials_unregistered() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.empty());

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.validateCredentials(" NIKKI@gmail.com ", "some password"));
        assertEquals("errors.email.unregistered", e.getMessage());

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(hashService);
    }

    @Test
    void validateCredentials_unconfirmed() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(false);

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.validateCredentials(" NIKKI@gmail.com ", "some password"));
        assertEquals("errors.email.unconfirmed", e.getMessage());

        verify(userProfileRepository).findByEmail("nikki@gmail.com");
        verifyNoInteractions(hashService);
    }

    @Test
    void validateCredentials_incorrectCredentials() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.salt()).thenReturn("some salt");
        when(userProfile.hash()).thenReturn("some hash");
        when(hashService.hash(any(), any())).thenReturn("some other hash");

        FailedPreconditionException e = assertThrows(FailedPreconditionException.class, () -> target.validateCredentials(" NIKKI@gmail.com ", "some password"));
        assertEquals("errors.credentials.incorrect", e.getMessage());

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService).hash("some password", "some salt");
    }

    @Test
    void validateCredentials() {
        when(userProfileRepository.findByEmail(any())).thenReturn(Optional.of(userProfile));
        when(userProfile.confirmed()).thenReturn(true);
        when(userProfile.salt()).thenReturn("some salt");
        when(userProfile.hash()).thenReturn("some hash");
        when(hashService.hash(any(), any())).thenReturn("some hash");

        target.validateCredentials(" NIKKI@gmail.com ", "some password");

        verify(userProfileRepository, times(2)).findByEmail("nikki@gmail.com");
        verify(hashService).hash("some password", "some salt");
    }
}
