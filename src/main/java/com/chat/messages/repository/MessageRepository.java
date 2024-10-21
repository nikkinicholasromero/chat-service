package com.chat.messages.repository;

import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, MessageId> {
}
