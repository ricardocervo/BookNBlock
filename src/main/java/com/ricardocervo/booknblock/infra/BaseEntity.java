package com.ricardocervo.booknblock.infra;

import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntity {

    public abstract UUID getId();

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseEntity other = (BaseEntity) obj;
        return Objects.equals(getId(), other.getId());
    }
}
