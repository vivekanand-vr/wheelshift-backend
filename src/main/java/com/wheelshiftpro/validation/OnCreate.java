package com.wheelshiftpro.validation;

/**
 * Validation group interface for CREATE operations.
 * Used to apply specific validations only during entity creation.
 * 
 * Example usage:
 * @NotNull(groups = OnCreate.class, message = "Field is required for creation")
 */
public interface OnCreate {
}
