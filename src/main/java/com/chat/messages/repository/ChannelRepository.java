package com.chat.messages.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChannelRepository extends CrudRepository<Channel, ChannelId> {
    List<Channel> findByIdIdIn(List<String> channelIds);
}
