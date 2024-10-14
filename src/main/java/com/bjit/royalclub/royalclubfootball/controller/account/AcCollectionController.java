package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.account.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.service.account.AcCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/collections")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcCollectionController {
    private final AcCollectionService service;

    /**
     * Save a new payment collection.
     *
     * @param paymentRequest The payment collection request containing player IDs, amount, and other details.
     * @return A ResponseEntity containing the created collection ID.
     */
    @PostMapping
    public ResponseEntity<Object> saveAcCollection(@Valid @RequestBody PaymentCollectionRequest paymentRequest) {
        Long collectionId = service.savePaymentCollection(paymentRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, collectionId);
    }

    /**
     * Get all payment collections.
     *
     * @return A ResponseEntity containing all payment collections.
     */
    @GetMapping
    public ResponseEntity<Object> getAllAcCollections() {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, service.getAllAcCollections());
    }

    /**
     * Get a specific payment collection by ID.
     *
     * @param id The ID of the collection to retrieve.
     * @return A ResponseEntity containing the payment collection.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAcCollection(@PathVariable Long id) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, service.getAcCollection(id));
    }

    /**
     * Record a monthly cost.
     *
     * @param costRequest The request body containing details about the monthly cost.
     * @return A ResponseEntity confirming that the cost has been recorded.
     */
    @PostMapping("/costs")
    public ResponseEntity<Object> recordCost(@Valid @RequestBody MonthlyCostRequest costRequest) {
        service.recordCost(costRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    /**
     * Update an existing payment collection by ID.
     *
     * @param id             The ID of the collection to update.
     * @param paymentRequest The updated payment collection request.
     * @return A ResponseEntity containing the updated collection ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAcCollection(@PathVariable Long id, @Valid @RequestBody PaymentCollectionRequest paymentRequest) {
        Long updatedCollectionId = service.updatePaymentCollection(id, paymentRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedCollectionId);
    }

    /**
     * Delete a payment collection by ID.
     *
     * @param id The ID of the collection to delete.
     * @return A ResponseEntity confirming that the collection has been deleted.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAcCollection(@PathVariable Long id) {
        service.deletePaymentCollection(id);
        return buildSuccessResponse(HttpStatus.NO_CONTENT, DELETE_OK);
    }
}
