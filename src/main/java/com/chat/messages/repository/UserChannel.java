package com.chat.messages.repository;

import com.chat.common.config.Generated;
import com.chat.common.repository.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

@Entity
@Table
public class UserChannel extends BaseEntity<UserChannelId> implements Serializable {
    @Id
    private UserChannelId id;

    @Generated
    protected UserChannel() {
        // Note: Required by JPA. Do not use.
    }

    @Override
    public UserChannelId id() {
        return id;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UserChannel other = (UserChannel) o;

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
                .toString();
    }
}
