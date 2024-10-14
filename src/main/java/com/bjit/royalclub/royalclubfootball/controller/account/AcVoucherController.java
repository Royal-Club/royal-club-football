package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherRequest;
import com.bjit.royalclub.royalclubfootball.service.account.AcVoucherService;
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
@RequestMapping("ac/vouchers")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcVoucherController {

    private final AcVoucherService service;

    /**
     * Retrieve all vouchers with optional detailed response.
     *
     * @param isDetailsResponse if true, include voucher details in the response
     * @return a list of all vouchers
     */
    @GetMapping
    public ResponseEntity<Object> getAllAcVouchers(
            @RequestParam(value = "isDetailsResponse", required = false) Boolean isDetailsResponse
    ) {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAllAcVouchers(isDetailsResponse));
    }

    /**
     * Retrieve a specific voucher by ID with optional detailed response.
     *
     * @param id                ID of the voucher
     * @param isDetailsResponse  if true, include voucher details in the response
     * @return the voucher response
     */
    @GetMapping("{id}")
    public ResponseEntity<Object> getAcVoucher(@PathVariable Long id,
                                               @RequestParam(value = "isDetailsResponse", required = false) Boolean isDetailsResponse
    ) {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAcVoucherResponse(id, isDetailsResponse));
    }

    /**
     * Save a new voucher.
     *
     * @param voucherRequest the request body containing voucher data
     * @return ID of the newly created voucher
     */
    @PostMapping
    public ResponseEntity<Object> saveVoucher(@Valid @RequestBody AcVoucherRequest voucherRequest) {
        Long id = service.saveVoucher(voucherRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, id);
    }

    /**
     * Update an existing voucher.
     *
     * @param id             ID of the voucher to update
     * @param voucherRequest the request body containing updated voucher data
     * @return ID of the updated voucher
     */
    @PutMapping("{id}")
    public ResponseEntity<Object> updateVoucher(@PathVariable Long id,
                                                @Valid @RequestBody AcVoucherRequest voucherRequest) {
        Long updatedId = service.updateVoucher(id, voucherRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedId);
    }

    /**
     * Delete a voucher by ID.
     *
     * @param id ID of the voucher to delete
     * @return success message
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteVoucher(@PathVariable Long id) {
        service.deleteVoucher(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }
}
