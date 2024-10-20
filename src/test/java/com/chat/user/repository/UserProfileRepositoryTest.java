package com.chat.user.repository;

import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserProfileRepositoryTest {
    private static final String EMAIL = "nikki@gmail.com";

    @Autowired
    private UserProfileRepository target;

    @Test
    void initial() {
        List<UserProfile> employees = IterableUtils.toList(target.findAll());
        assertThat(employees).isEmpty();
    }

    @Test
    void instantiate_UserProfileId_invalid() {
        assertThrows(IllegalArgumentException.class, () -> new UserProfileId(" "));
    }

    @Test
    void instantiate_UserProfile_regular() {
        String id = UUID.randomUUID().toString();
        String salt = UUID.randomUUID().toString().substring(0, 26);
        String hash = UUID.randomUUID().toString();
        String confirmationCode = UUID.randomUUID().toString();
        String firstName = "Nikki Nicholas";
        String lastName = "Romero";

        assertThat(target.findByEmail(EMAIL)).isEmpty();

        UserProfile expected = UserProfile.regular(
                new UserProfileId(id),
                EMAIL,
                salt,
                hash,
                confirmationCode,
                firstName,
                lastName);

        target.save(expected);

        UserProfile actual = target.findByEmail(EMAIL).orElseThrow();
        assertEquals(expected, actual);
        assertEquals(id, actual.id().id());
        assertEquals(EMAIL, actual.email());
        assertEquals(salt, actual.salt());
        assertEquals(hash, actual.hash());
        assertEquals(confirmationCode, actual.confirmationCode());
        assertFalse(actual.confirmed());
        assertNull(actual.passwordResetCode());
        assertEquals(firstName, actual.firstName());
        assertEquals(lastName, actual.lastName());
    }

    @Test
    void instantiate_UserProfile_social() {
        String id = UUID.randomUUID().toString();
        String firstName = "Nikki Nicholas";
        String lastName = "Romero";

        assertThat(target.findByEmail(EMAIL)).isEmpty();

        UserProfile expected = UserProfile.social(
                new UserProfileId(id),
                EMAIL,
                firstName,
                lastName);

        target.save(expected);

        UserProfile actual = target.findByEmail(EMAIL).orElseThrow();
        assertEquals(expected, actual);
        assertEquals(id, actual.id().id());
        assertEquals(EMAIL, actual.email());
        assertNull(actual.salt());
        assertNull(actual.hash());
        assertNull(actual.confirmationCode());
        assertTrue(actual.confirmed());
        assertNull(actual.passwordResetCode());
        assertEquals(firstName, actual.firstName());
        assertEquals(lastName, actual.lastName());
    }

    @Test
    void confirmRegistration() {
        UserProfile expected = regularUserProfile();

        expected.confirmRegistration();
        target.save(expected);

        UserProfile actual = target.findByEmail(EMAIL).orElseThrow();
        assertNull(actual.confirmationCode());
        assertTrue(actual.confirmed());
    }

    @Test
    void setPasswordResetCode() {
        UserProfile expected = regularUserProfile();

        String passwordResetCode = "some password reset code";
        expected.setPasswordResetCode(passwordResetCode);
        assertEquals(passwordResetCode, expected.passwordResetCode());

        String newSalt = "some new salt";
        String newHash = "some new hash";
        expected.updatePassword(newSalt, newHash);
        target.save(expected);

        UserProfile actual = target.findByEmail(EMAIL).orElseThrow();
        assertEquals(newSalt, actual.salt());
        assertEquals(newHash, actual.hash());
        assertNull(actual.passwordResetCode());
    }

    @Test
    void updateProfile() {
        UserProfile expected = regularUserProfile();

        expected.updateProfile("New First Name", "New Last Name");
        target.save(expected);

        UserProfile actual = target.findByEmail(EMAIL).orElseThrow();
        assertEquals("New First Name", actual.firstName());
        assertEquals("New Last Name", actual.lastName());
    }

    private UserProfile regularUserProfile() {
        String id = UUID.randomUUID().toString();
        String salt = UUID.randomUUID().toString().substring(0, 26);
        String hash = UUID.randomUUID().toString();
        String confirmationCode = UUID.randomUUID().toString();
        String firstName = "Nikki Nicholas";
        String lastName = "Romero";

        return UserProfile.regular(
                new UserProfileId(id),
                EMAIL,
                salt,
                hash,
                confirmationCode,
                firstName,
                lastName);
    }
}
