package com.chat.common.repository;

import com.chat.common.config.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@Entity
@Table
public class UserProfile extends BaseEntity<UserProfileId> implements Serializable {
    @Id
    private UserProfileId id;

    @Column(name = "email")
    private String email;

    @Column(name = "salt")
    private String salt;

    @Column(name = "hash")
    private String hash;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "confirmed")
    private boolean confirmed;

    @Column(name = "password_reset_code")
    private String passwordResetCode;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Generated
    protected UserProfile() {
        // Note: Required by JPA. Do not use.
    }

    public static UserProfile regular(
            UserProfileId id,
            String email,
            String salt,
            String hash,
            String confirmationCode,
            String firstName,
            String lastName) {
        UserProfile userProfile = new UserProfile();
        userProfile.id = id;
        userProfile.email = email;
        userProfile.salt = salt;
        userProfile.hash = hash;
        userProfile.confirmationCode = confirmationCode;
        userProfile.confirmed = false;
        userProfile.firstName = firstName;
        userProfile.lastName = lastName;
        return userProfile;
    }

    public static UserProfile social(
            UserProfileId id,
            String email,
            String firstName,
            String lastName) {
        UserProfile userProfile = new UserProfile();
        userProfile.id = id;
        userProfile.email = email;
        userProfile.confirmed = true;
        userProfile.firstName = firstName;
        userProfile.lastName = lastName;
        return userProfile;
    }

    @Override
    public UserProfileId id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String salt() {
        return salt;
    }

    public String hash() {
        return hash;
    }

    public String confirmationCode() {
        return confirmationCode;
    }

    public boolean confirmed() {
        return confirmed;
    }

    public String passwordResetCode() {
        return passwordResetCode;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public void confirmRegistration() {
        this.confirmationCode = null;
        this.confirmed = true;
    }

    public void setPasswordResetCode(String passwordResetCode) {
        this.passwordResetCode = passwordResetCode;
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void updatePassword(String salt, String hash) {
        this.salt = salt;
        this.hash = hash;
        this.passwordResetCode = null;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserProfile other = (UserProfile) o;

        return new EqualsBuilder()
                .append(id, other.id)
                .isEquals();
    }

    @Override
    @Generated
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    @Generated
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("email", email)
                .append("salt", "********")
                .append("hash", "********")
                .append("confirmationCode", "********")
                .append("confirmed", confirmed)
                .append("passwordResetCode", "********")
                .append("firstName", firstName)
                .append("lastName", lastName)
                .toString();
    }
}
