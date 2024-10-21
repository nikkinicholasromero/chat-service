package com.chat.messages.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserChannelRepository extends CrudRepository<UserChannel, UserChannelId> {
    List<UserChannel> findByIdUserId(String id);
}
