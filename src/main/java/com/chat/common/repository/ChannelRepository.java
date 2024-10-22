package com.chat.common.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChannelRepository extends CrudRepository<Channel, String> {
    List<Channel> findByIdIn(List<String> channelIds);
}
