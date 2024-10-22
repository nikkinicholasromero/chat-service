package com.chat.common.repository;

import com.chat.common.config.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@Embeddable
public class UserChannelId implements Serializable {
    @Column(name = "userId")
    private String userId;

    @Column(name = "channelId")
    private String channelId;

    @Generated
    protected UserChannelId() {
        // Note: Required by JPA. Do not use.
    }

    public UserChannelId(String userId, String channelId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is required");
        }

        this.userId = userId.strip();

        if (StringUtils.isBlank(channelId)) {
            throw new IllegalArgumentException("channelId is required");
        }

        this.channelId = channelId.strip();
    }

    public String userId() {
        return userId;
    }

    public String channelId() {
        return channelId;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserChannelId other = (UserChannelId) o;

        return new EqualsBuilder()
                .append(userId, other.userId)
                .append(channelId, other.channelId)
                .isEquals();
    }

    @Override
    @Generated
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userId)
                .append(channelId)
                .toHashCode();
    }

    @Override
    @Generated
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .append("channelId", channelId)
                .toString();
    }
}
