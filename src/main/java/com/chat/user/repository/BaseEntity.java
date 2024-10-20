package com.chat.user.repository;

import com.chat.common.config.Generated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Generated
@MappedSuperclass
public abstract class BaseEntity<T> implements Persistable<T> {
    @Transient
    private boolean persisted = false;

    @Override
    public boolean isNew() {
        return !persisted;
    }

    @PostPersist
    @PostLoad
    protected void setPersisted() {
        this.persisted = true;
    }

    public abstract T id();

    @Override
    public T getId() {
        return id();
    }
}
