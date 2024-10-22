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
public class Channel extends BaseEntity<String> implements Serializable {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Generated
    protected Channel() {
        // Note: Required by JPA. Do not use.
    }

    @Override
    public String id() {
        return id;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Channel other = (Channel) o;

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
                .append("createdAt", createdAt)
                .toString();
    }
}
