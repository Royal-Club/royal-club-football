package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcVoucherTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/voucher-types")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcVoucherTypeController {

    private final AcVoucherTypeService service;

    /**
     * Get all voucher types.
     *
     * @return list of voucher types
     */
    @GetMapping
    public ResponseEntity<Object> getAcVoucherTypes() {
        List<AcVoucherTypeResponse> voucherTypeResponses = service.getAcVoucherTypes();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, voucherTypeResponses);
    }

    /**
     * Get voucher type by ID.
     *
     * @param id the ID of the voucher type
     * @return the voucher type response
     */
    @GetMapping("{id}")
    public ResponseEntity<Object> getAcVoucherTypeById(@PathVariable Long id) {
        AcVoucherTypeResponse voucherTypeResponse = service.getAcVoucherTypeResponse(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, voucherTypeResponse);
    }

    /**
     * Save a new voucher type.
     *
     * @param request the request body containing voucher type data
     * @return ID of the newly created voucher type
     */
    @PostMapping
    public ResponseEntity<Object> saveAcVoucherType(@Valid @RequestBody AcVoucherTypeRequest request) {
        Long id = service.saveAcVoucherType(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, id);
    }

    /**
     * Update an existing voucher type by ID.
     *
     * @param id      the ID of the voucher type to update
     * @param request the request body containing updated voucher type data
     * @return ID of the updated voucher type
     */
    @PutMapping("{id}")
    public ResponseEntity<Object> updateAcVoucherType(@PathVariable Long id,
                                                      @Valid @RequestBody AcVoucherTypeRequest request) {
        Long updatedId = service.updateAcVoucherType(id, request);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedId);
    }

    /**
     * Delete a voucher type by ID.
     *
     * @param id the ID of the voucher type to delete
     * @return success message
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteAcVoucherType(@PathVariable Long id) {
        service.deleteAcVoucherType(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }
}
