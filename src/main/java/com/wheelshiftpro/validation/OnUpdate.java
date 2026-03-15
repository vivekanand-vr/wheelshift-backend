package com.wheelshiftpro.validation;

/**
 * Validation group interface for UPDATE operations.
 * Used to apply specific validations only during entity updates.
 * 
 * Example usage:
 * @NotNull(groups = OnUpdate.class, message = "Field cannot be null during update")
 */
public interface OnUpdate {
}
