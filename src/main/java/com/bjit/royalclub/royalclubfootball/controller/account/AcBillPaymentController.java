package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcBillPaymentRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcBillPaymentResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcBillPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/bill-payments")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcBillPaymentController {

    private final AcBillPaymentService service;

    /**
     * Create a new bill payment.
     *
     * @param paymentRequest The request body containing the bill payment data.
     * @return A response with the created bill payment ID.
     */
    @PostMapping
    public ResponseEntity<Object> saveAcBillPayment(
            @Valid @RequestBody AcBillPaymentRequest paymentRequest) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, service.save(paymentRequest));
    }

    @GetMapping("/ajax-collections")
    public ResponseEntity<Object> getAcBillPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Page<AcBillPaymentResponse> result = service.getPaginatedAcBillPayments(page, size, sortBy, order, year, month);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Retrieve all bill payments.
     *
     * @return A response containing a list of all bill payments.
     */
    @GetMapping
    public ResponseEntity<Object> getAllAcBillPayments() {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, service.getAllBillPayments());
    }

    /**
     * Retrieve a bill payment by its ID.
     *
     * @param id The ID of the bill payment to retrieve.
     * @return A response containing the bill payment data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAcBillPaymentById(@PathVariable Long id) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, service.getAcBillPaymentResponse(service.getAcBillPaymentEntity(id)));
    }

    /**
     * Update an existing bill payment by ID.
     *
     * @param id The ID of the bill payment to update.
     * @param paymentRequest The request body containing the updated bill payment data.
     * @return A response with the updated bill payment ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAcBillPayment(
            @PathVariable Long id,
            @Valid @RequestBody AcBillPaymentRequest paymentRequest) {
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, service.update(id, paymentRequest));
    }

    /**
     * Delete a bill payment by ID.
     *
     * @param id The ID of the bill payment to delete.
     * @return A response confirming the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAcBillPayment(@PathVariable Long id) {
        service.delete(id);
        return buildSuccessResponse(HttpStatus.NO_CONTENT, DELETE_OK, null);
    }
}
