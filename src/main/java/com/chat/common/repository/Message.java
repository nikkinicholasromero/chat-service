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
import java.time.LocalDateTime;

@Entity
@Table
public class Message extends BaseEntity<MessageId> implements Serializable {
    @Id
    private MessageId id;

    @Column(name = "content")
    private String content;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "channelId")
    private String channelId;

    @Column(name = "senderId")
    private String senderId;

    @Generated
    protected Message() {
        // Note: Required by JPA. Do not use.
    }

    @Override
    public MessageId id() {
        return id;
    }

    public String content() {
        return content;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public String channelId() {
        return channelId;
    }

    public String senderId() {
        return senderId;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Message other = (Message) o;

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
                .append("content", content)
                .append("createdAt", createdAt)
                .append("channelId", channelId)
                .append("senderId", senderId)
                .toString();
    }
}
