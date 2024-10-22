package com.chat.common.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, String> {
    List<Message> findByChannelIdIn(List<String> channelIds);
}
