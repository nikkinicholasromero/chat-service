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
public class ChannelId implements Serializable {
    @Column(name = "id")
    private String id;

    @Generated
    protected ChannelId() {
        // Note: Required by JPA. Do not use.
    }

    public ChannelId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("id is required");
        }

        this.id = id.strip();
    }

    public String id() {
        return id;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChannelId other = (ChannelId) o;

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
