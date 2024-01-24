package com.ricardocervo.booknblock.property;

import java.util.UUID;

public interface PropertyService {
    public Property createProperty(Property property);

    public Property getPropertyOrThrowException(UUID propertyId);
}

