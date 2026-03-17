package com.wheelshiftpro.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that at least one vehicle ID (carId or motorcycleId) is provided.
 * Used for entities that can be associated with either cars or motorcycles.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VehicleIdRequiredValidator.class)
@Documented
public @interface VehicleIdRequired {
    
    String message() default "Either carId or motorcycleId must be provided";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Field name for car ID
     */
    String carIdField() default "carId";
    
    /**
     * Field name for motorcycle ID
     */
    String motorcycleIdField() default "motorcycleId";
}
