package com.wheelshiftpro.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * Validator for @VehicleIdRequired annotation.
 * Ensures that at least one vehicle ID (carId or motorcycleId) is provided.
 */
public class VehicleIdRequiredValidator implements ConstraintValidator<VehicleIdRequired, Object> {

    private String carIdField;
    private String motorcycleIdField;

    @Override
    public void initialize(VehicleIdRequired constraintAnnotation) {
        this.carIdField = constraintAnnotation.carIdField();
        this.motorcycleIdField = constraintAnnotation.motorcycleIdField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null objects
        }

        try {
            Object carId = getFieldValue(value, carIdField);
            Object motorcycleId = getFieldValue(value, motorcycleIdField);

            // At least one must be non-null
            boolean valid = (carId != null || motorcycleId != null);

            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Either " + carIdField + " or " + motorcycleIdField + " must be provided"
                ).addPropertyNode(carIdField).addConstraintViolation();
            }

            return valid;
        } catch (Exception e) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
