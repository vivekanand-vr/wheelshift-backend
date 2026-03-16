package com.wheelshiftpro.validation;

import com.wheelshiftpro.enums.VehicleType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * Validator for @ConditionalVehicleId annotation.
 * Ensures that the correct vehicle ID is provided based on vehicleType.
 */
public class ConditionalVehicleIdValidator implements ConstraintValidator<ConditionalVehicleId, Object> {

    private String vehicleTypeField;
    private String carIdField;
    private String motorcycleIdField;

    @Override
    public void initialize(ConditionalVehicleId constraintAnnotation) {
        this.vehicleTypeField = constraintAnnotation.vehicleTypeField();
        this.carIdField = constraintAnnotation.carIdField();
        this.motorcycleIdField = constraintAnnotation.motorcycleIdField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null objects
        }

        try {
            VehicleType vehicleType = (VehicleType) getFieldValue(value, vehicleTypeField);
            Object carId = getFieldValue(value, carIdField);
            Object motorcycleId = getFieldValue(value, motorcycleIdField);

            // If no vehicle type specified, at least one ID must be present
            if (vehicleType == null) {
                return (carId != null || motorcycleId != null);
            }

            boolean valid = true;
            String errorMessage = null;

            // Validate based on vehicle type
            if (vehicleType == VehicleType.CAR) {
                if (carId == null) {
                    valid = false;
                    errorMessage = "carId is required when vehicleType is CAR";
                }
            } else if (vehicleType == VehicleType.MOTORCYCLE) {
                if (motorcycleId == null) {
                    valid = false;
                    errorMessage = "motorcycleId is required when vehicleType is MOTORCYCLE";
                }
            }

            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(errorMessage)
                    .addPropertyNode(vehicleType == VehicleType.CAR ? carIdField : motorcycleIdField)
                    .addConstraintViolation();
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
