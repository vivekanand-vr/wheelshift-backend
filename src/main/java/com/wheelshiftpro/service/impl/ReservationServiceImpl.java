package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.ReservationMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.ReservationRepository;
import com.wheelshiftpro.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final ReservationMapper reservationMapper;

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {
        log.debug("Creating reservation for car ID: {}", request.getCarId());

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", request.getCarId()));

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new BusinessException("Car is not available for reservation. Current status: " + car.getStatus(), 
                    "CAR_NOT_AVAILABLE");
        }

        if (!clientRepository.existsById(request.getClientId())) {
            throw new ResourceNotFoundException("Client", "id", request.getClientId());
        }

        // Check for existing active reservation
        if (reservationRepository.existsByCarId(request.getCarId())) {
            throw new BusinessException("Car already has an active reservation", "RESERVATION_EXISTS");
        }

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Update car status
        car.setStatus(CarStatus.RESERVED);
        carRepository.save(car);

        Reservation saved = reservationRepository.save(reservation);

        log.info("Created reservation with ID: {}", saved.getId());
        return reservationMapper.toResponse(saved);
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        log.debug("Updating reservation ID: {}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        reservationMapper.updateEntityFromRequest(request, reservation);
        Reservation updated = reservationRepository.save(reservation);

        log.info("Updated reservation ID: {}", id);
        return reservationMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        log.debug("Fetching reservation ID: {}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getAllReservations(int page, int size) {
        log.debug("Fetching all reservations - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDate").descending());
        Page<Reservation> reservationsPage = reservationRepository.findAll(pageable);

        return buildPageResponse(reservationsPage);
    }

    public void deleteReservation(Long id) {
        log.debug("Deleting reservation ID: {}", id);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Revert car status if reservation is active
        if (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.PENDING) {
            Car car = reservation.getCar();
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }

        reservationRepository.delete(reservation);
        log.info("Deleted reservation ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getReservationsByStatus(ReservationStatus status, int page, int size) {
        log.debug("Fetching reservations by status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDate").descending());
        Page<Reservation> reservationsPage = reservationRepository.findByStatus(status, pageable);

        return buildPageResponse(reservationsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getReservationsByClient(Long clientId, int page, int size) {
        log.debug("Fetching reservations for client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDate").descending());
        Page<Reservation> reservationsPage = reservationRepository.findByClientId(clientId, pageable);

        return buildPageResponse(reservationsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getActiveReservations(int page, int size) {
        log.debug("Fetching active reservations");

        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDate").descending());
        Page<Reservation> reservationsPage = reservationRepository.findByStatus(ReservationStatus.CONFIRMED, pageable);

        return buildPageResponse(reservationsPage);
    }

    @Override
    public ReservationResponse updateReservationStatus(Long id, ReservationStatus status) {
        log.debug("Updating reservation ID: {} status to: {}", id, status);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);

        log.info("Updated reservation ID: {} status to: {}", id, status);
        return reservationMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getExpiringReservations(int daysUntilExpiry, int page, int size) {
        log.debug("Fetching reservations expiring in {} days", daysUntilExpiry);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(daysUntilExpiry);
        List<Reservation> reservations = reservationRepository.findReservationsExpiringSoon(startTime, endTime);
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate"));
        Page<Reservation> reservationsPage = new org.springframework.data.domain.PageImpl<>(reservations, pageable, reservations.size());

        return buildPageResponse(reservationsPage);
    }

    @Override
    public SaleResponse convertToSale(Long reservationId, Long employeeId) {
        log.debug("Converting reservation ID: {} to sale", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed reservations can be converted to sales", "INVALID_RESERVATION_STATUS");
        }

        // This will be implemented when SaleService is integrated
        throw new BusinessException("Sale conversion not yet implemented", "NOT_IMPLEMENTED");
    }

    @Override
    public ReservationResponse cancelReservation(Long reservationId) {
        log.debug("Cancelling reservation ID: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed reservations can be cancelled", "INVALID_RESERVATION_STATUS");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Revert car status
        Car car = reservation.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);

        Reservation updated = reservationRepository.save(reservation);

        log.info("Cancelled reservation ID: {}", reservationId);
        return reservationMapper.toResponse(updated);
    }

    @Override
    public void expireReservations() {
        log.debug("Expiring past-due reservations");

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(now);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);

            // Revert car status
            Car car = reservation.getCar();
            if (car.getStatus() == CarStatus.RESERVED) {
                car.setStatus(CarStatus.AVAILABLE);
                carRepository.save(car);
            }

            reservationRepository.save(reservation);
        }

        log.info("Expired {} reservations", expiredReservations.size());
    }

    @Override
    public ReservationResponse updateDepositStatus(Long reservationId, boolean depositPaid) {
        log.debug("Updating deposit status for reservation ID: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        reservation.setDepositPaid(depositPaid);
        Reservation updated = reservationRepository.save(reservation);

        log.info("Updated deposit status for reservation ID: {}", reservationId);
        return reservationMapper.toResponse(updated);
    }

    private PageResponse<ReservationResponse> buildPageResponse(Page<Reservation> page) {
        List<ReservationResponse> content = reservationMapper.toResponseList(page.getContent());

        return PageResponse.<ReservationResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
