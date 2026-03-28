package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.ReservationRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.ReservationResponse;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Client;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.ReservationMapper;
import com.wheelshiftpro.repository.CarRepository;
import com.wheelshiftpro.repository.ClientRepository;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.ReservationRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import com.wheelshiftpro.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationMapper reservationMapper;
    private final AuditService auditService;

    @Override
    public ReservationResponse createReservation(ReservationRequest request) {
        log.debug("Creating reservation for car ID: {}", request.getCarId());

        // Validate expiry date
        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Expiry date must be in the future", "INVALID_EXPIRY_DATE");
        }

        // Validate deposit amount
        if (request.getDepositAmount() != null && request.getDepositAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("Deposit amount cannot be negative", "INVALID_DEPOSIT_AMOUNT");
        }

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", request.getCarId()));

        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new BusinessException("Car is not available for reservation. Current status: " + car.getStatus(), 
                    "CAR_NOT_AVAILABLE");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));

        // Check for existing active reservation
        if (reservationRepository.existsByCarId(request.getCarId())) {
            throw new BusinessException("Car already has an active reservation", "RESERVATION_EXISTS");
        }

        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setCar(car);
        reservation.setClient(client);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Update car status
        car.setStatus(CarStatus.RESERVED);
        carRepository.save(car);

        Reservation saved = reservationRepository.save(reservation);

        auditService.log(AuditCategory.RESERVATION, saved.getId(), "CREATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Car ID: " + car.getId() + ", Client: " + client.getName());

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

        auditService.log(AuditCategory.RESERVATION, id, "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Reservation updated");

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

        auditService.log(AuditCategory.RESERVATION, id, "DELETE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "Reservation deleted");

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

        ReservationStatus previousStatus = reservation.getStatus();
        reservation.setStatus(status);

        // Revert car status if cancelled or expired
        if (status == ReservationStatus.CANCELLED || status == ReservationStatus.EXPIRED) {
            if (previousStatus == ReservationStatus.CONFIRMED || previousStatus == ReservationStatus.PENDING) {
                Car car = reservation.getCar();
                car.setStatus(CarStatus.AVAILABLE);
                carRepository.save(car);
            }
        }

        Reservation updated = reservationRepository.save(reservation);

        auditService.log(AuditCategory.RESERVATION, id, "STATUS_CHANGE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "From " + previousStatus + " to " + status);

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

        if (!reservation.getDepositPaid()) {
            throw new BusinessException("Deposit must be paid before converting to sale", "DEPOSIT_NOT_PAID");
        }

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        // This will be implemented when SaleService is integrated
        throw new BusinessException("Sale conversion not yet implemented", "NOT_IMPLEMENTED");
    }

    @Override
    public ReservationResponse cancelReservation(Long reservationId) {
        log.debug("Cancelling reservation ID: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Only confirmed or pending reservations can be cancelled", "INVALID_RESERVATION_STATUS");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Revert car status
        Car car = reservation.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);

        Reservation updated = reservationRepository.save(reservation);

        auditService.log(AuditCategory.RESERVATION, reservationId, "STATUS_CHANGE", AuditLevel.HIGH,
                resolveCurrentEmployee(), "Reservation cancelled");

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

            auditService.log(AuditCategory.RESERVATION, reservation.getId(), "STATUS_CHANGE", AuditLevel.HIGH,
                    null, "Reservation expired (automated)");
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

        auditService.log(AuditCategory.RESERVATION, reservationId, "UPDATE", AuditLevel.REGULAR,
                resolveCurrentEmployee(), "Deposit status: " + depositPaid);

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

    private Employee resolveCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeUserDetails u) {
            return employeeRepository.getReferenceById(u.getId());
        }
        return null;
    }
}
