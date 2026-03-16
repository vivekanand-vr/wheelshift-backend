package com.wheelshiftpro.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that the correct vehicle ID is provided based on vehicleType.
 * If vehicleType is CAR, carId is required.
 * If vehicleType is MOTORCYCLE, motorcycleId is required.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalVehicleIdValidator.class)
@Documented
public @interface ConditionalVehicleId {
    
    String message() default "Vehicle ID must match the specified vehicle type";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Field name for vehicle type
     */
    String vehicleTypeField() default "vehicleType";
    
    /**
     * Field name for car ID
     */
    String carIdField() default "carId";
    
    /**
     * Field name for motorcycle ID
     */
    String motorcycleIdField() default "motorcycleId";
}
