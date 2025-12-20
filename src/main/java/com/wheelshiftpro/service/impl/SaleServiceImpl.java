package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.SaleRequest;
import com.wheelshiftpro.dto.response.PageResponse;
import com.wheelshiftpro.dto.response.SaleResponse;
import com.wheelshiftpro.entity.Car;
import com.wheelshiftpro.entity.Sale;
import com.wheelshiftpro.enums.CarStatus;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.exception.BusinessException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.SaleMapper;
import com.wheelshiftpro.repository.*;
import com.wheelshiftpro.service.ClientService;
import com.wheelshiftpro.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;
    private final SaleMapper saleMapper;
    private final ClientService clientService;

    @Override
    public SaleResponse createSale(SaleRequest request) {
        log.debug("Creating sale for car ID: {}", request.getCarId());

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car", "id", request.getCarId()));

        if (car.getStatus() == CarStatus.SOLD) {
            throw new BusinessException("Car is already sold", "CAR_ALREADY_SOLD");
        }

        if (!clientRepository.existsById(request.getClientId())) {
            throw new ResourceNotFoundException("Client", "id", request.getClientId());
        }

        if (!employeeRepository.existsById(request.getEmployeeId())) {
            throw new ResourceNotFoundException("Employee", "id", request.getEmployeeId());
        }

        Sale sale = saleMapper.toEntity(request);

        // Update car status
        car.setStatus(CarStatus.SOLD);
        carRepository.save(car);

        // If there's an active reservation, mark it as converted
        reservationRepository.findByCarId(car.getId())
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.CONFIRMED);
                    reservationRepository.save(reservation);
                });

        // Increment client purchase count
        clientService.incrementPurchaseCount(request.getClientId());

        Sale saved = saleRepository.save(sale);

        log.info("Created sale with ID: {}", saved.getId());
        return saleMapper.toResponse(saved);
    }

    @Override
    public SaleResponse updateSale(Long id, SaleRequest request) {
        log.debug("Updating sale ID: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));

        saleMapper.updateEntityFromRequest(request, sale);
        Sale updated = saleRepository.save(sale);

        log.info("Updated sale ID: {}", id);
        return saleMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        log.debug("Fetching sale ID: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));

        return saleMapper.toResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleByCarId(Long carId) {
        log.debug("Fetching sale for car ID: {}", carId);

        Sale sale = saleRepository.findByCarId(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "carId", carId));

        return saleMapper.toResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getAllSales(int page, int size) {
        log.debug("Fetching all sales - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        Page<Sale> salesPage = saleRepository.findAll(pageable);

        return buildPageResponse(salesPage);
    }

    @Override
    public void deleteSale(Long id) {
        log.debug("Deleting sale ID: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", id));

        // Optionally revert car status
        Car car = sale.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);

        saleRepository.delete(sale);
        log.info("Deleted sale ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getSalesByClient(Long clientId, int page, int size) {
        log.debug("Fetching sales for client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        Page<Sale> salesPage = saleRepository.findByClientId(clientId, pageable);

        return buildPageResponse(salesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getSalesByEmployee(Long employeeId, int page, int size) {
        log.debug("Fetching sales for employee ID: {}", employeeId);

        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        Page<Sale> salesPage = saleRepository.findByEmployeeId(employeeId, pageable);

        return buildPageResponse(salesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Fetching sales between {} and {}", startDate, endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by("saleDate").descending());
        Page<Sale> salesPage = saleRepository.findBySaleDateBetween(startDate, endDate, pageable);

        return buildPageResponse(salesPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating total revenue between {} and {}", startDate, endDate);

        BigDecimal total = saleRepository.calculateTotalRevenue(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCommission(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating total commission between {} and {}", startDate, endDate);

        BigDecimal total = saleRepository.calculateTotalCommission(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }



    @Override
    @Transactional(readOnly = true)
    public Object getEmployeePerformance(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating employee performance between {} and {}", startDate, endDate);

        // Return basic statistics for all employees
        return Map.of(
                "message", "Employee performance metrics",
                "startDate", startDate,
                "endDate", endDate
        );
    }

    private PageResponse<SaleResponse> buildPageResponse(Page<Sale> page) {
        List<SaleResponse> content = saleMapper.toResponseList(page.getContent());

        return PageResponse.<SaleResponse>builder()
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
