package com.wheelshiftpro.service;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.enums.ReservationStatus;

/**
 * Service interface for Reservation operations.
 * Manages vehicle reservations and lifecycle.
 */
public interface ReservationService {

    /**
     * Creates a new reservation for a car.
     * Validates car availability and uniqueness.
     *
     * @param request the reservation data
     * @return created reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if car or client not found
     * @throws com.wheelshiftpro.exception.BusinessException if car already reserved or sold
     */
    ReservationResponse createReservation(ReservationRequest request);

    /**
     * Updates an existing reservation.
     *
     * @param id the reservation ID
     * @param request updated reservation data
     * @return updated reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     */
    ReservationResponse updateReservation(Long id, ReservationRequest request);

    /**
     * Retrieves a reservation by ID.
     *
     * @param id the reservation ID
     * @return reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     */
    ReservationResponse getReservationById(Long id);

    /**
     * Retrieves all reservations with pagination.
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return paginated reservation responses
     */
    PageResponse<ReservationResponse> getAllReservations(int page, int size);

    /**
     * Retrieves reservations by status.
     *
     * @param status the reservation status
     * @param page page number
     * @param size page size
     * @return paginated reservation responses
     */
    PageResponse<ReservationResponse> getReservationsByStatus(ReservationStatus status, int page, int size);

    /**
     * Retrieves reservations for a specific client.
     *
     * @param clientId the client ID
     * @param page page number
     * @param size page size
     * @return paginated reservation responses
     */
    PageResponse<ReservationResponse> getReservationsByClient(Long clientId, int page, int size);

    /**
     * Retrieves active reservations (pending or confirmed).
     *
     * @param page page number
     * @param size page size
     * @return paginated active reservations
     */
    PageResponse<ReservationResponse> getActiveReservations(int page, int size);

    /**
     * Updates reservation status.
     *
     * @param id the reservation ID
     * @param status new status
     * @return updated reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     */
    ReservationResponse updateReservationStatus(Long id, ReservationStatus status);

    /**
     * Updates deposit payment status.
     *
     * @param id the reservation ID
     * @param paid payment status
     * @return updated reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     */
    ReservationResponse updateDepositStatus(Long id, boolean paid);

    /**
     * Cancels a reservation.
     * Reverts car status to AVAILABLE.
     *
     * @param id the reservation ID
     * @return cancelled reservation response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     */
    ReservationResponse cancelReservation(Long id);

    /**
     * Converts a reservation to a sale.
     * Validates reservation can be converted.
     *
     * @param reservationId the reservation ID
     * @param employeeId the employee handling the sale
     * @return sale response
     * @throws com.wheelshiftpro.exception.ResourceNotFoundException if reservation not found
     * @throws com.wheelshiftpro.exception.BusinessException if conversion not allowed
     */
    Object convertToSale(Long reservationId, Long employeeId);

    /**
     * Checks and expires reservations past their expiry date.
     * Scheduled job method.
     */
    void expireReservations();
}
