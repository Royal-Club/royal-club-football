package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcChartRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcChartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/charts")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcChartController {

    private final AcChartService service;

    /**
     * Retrieve all charts.
     *
     * @return A list of all charts.
     */
    @GetMapping
    public ResponseEntity<Object> getAcCharts() {
        List<AcChartResponse> chartResponses = service.getAcCharts();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, chartResponses);
    }

    /**
     * Retrieve a chart by its ID.
     *
     * @param id The ID of the chart to retrieve.
     * @return The requested chart's details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAcChartById(@PathVariable Long id) {
        AcChartResponse chartResponse = service.getAcChartResponse(service.getAcChartById(id));
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, chartResponse);
    }

    /**
     * Create a new chart.
     *
     * @param chartRequest The request body containing the chart details.
     * @return The ID of the created chart.
     */
    @PostMapping
    public ResponseEntity<Object> saveAcChart(@Valid @RequestBody AcChartRequest chartRequest) {
        Long chartId = service.saveChart(chartRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, chartId);
    }

    /**
     * Update an existing chart by ID.
     *
     * @param id           The ID of the chart to update.
     * @param chartRequest The request body containing the updated chart details.
     * @return The ID of the updated chart.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAcChart(
            @PathVariable Long id,
            @Valid @RequestBody AcChartRequest chartRequest) {
        Long updatedChartId = service.updateChart(id, chartRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedChartId);
    }

    /**
     * Delete a chart by its ID.
     *
     * @param id The ID of the chart to delete.
     * @return A confirmation of the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAcChart(@PathVariable Long id) {
        service.deleteChart(id);
        return buildSuccessResponse(HttpStatus.NO_CONTENT, DELETE_OK, null);
    }
}
