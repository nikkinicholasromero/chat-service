package com.chat.common.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserProfileRepository extends CrudRepository<UserProfile, String> {
    Optional<UserProfile> findByEmail(String email);
}
