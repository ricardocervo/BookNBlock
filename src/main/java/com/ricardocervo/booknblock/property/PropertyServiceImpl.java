package com.ricardocervo.booknblock.property;

import com.ricardocervo.booknblock.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{
    private final PropertyRepository propertyRepository;

    @Override
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    @Override
    public Property getPropertyOrThrowException(UUID propertyId) {
        return propertyRepository.findById(propertyId).orElseThrow(() -> new ResourceNotFoundException("Property not found with id " + propertyId));
    }

}
