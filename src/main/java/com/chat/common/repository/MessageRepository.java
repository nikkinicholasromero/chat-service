package com.chat.common.repository;

import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, MessageId> {
}
