package com.chat.user.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserProfileRepository extends CrudRepository<UserProfile, UserProfileId> {
    Optional<UserProfile> findByEmail(String email);
}
