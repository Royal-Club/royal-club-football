package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;

import java.util.List;

public interface AcChartService {

    /**
     * Retrieves all AcChart entries.
     *
     * @return list of AcChartResponse
     */
    List<AcChartResponse> getAcCharts();

    /**
     * Retrieves AcChart by ID.
     *
     * @param id ID of the AcChart
     * @return AcChart entity
     */
    AcChart getAcChartById(Long id);

    /**
     * Converts AcChart entity to AcChartResponse.
     *
     * @param acChart AcChart entity
     * @return AcChartResponse DTO
     */
    AcChartResponse getAcChartResponse(AcChart acChart);

    /**
     * Saves a new AcChart.
     *
     * @param request AcChartRequest containing the chart details
     * @return ID of the newly saved AcChart
     */
    Long saveChart(AcChartRequest request);

    /**
     * Updates an existing AcChart by ID.
     *
     * @param id      ID of the AcChart to update
     * @param request AcChartRequest containing the updated chart details
     * @return ID of the updated AcChart
     */
    Long updateChart(Long id, AcChartRequest request);

    /**
     * Deletes an AcChart by ID, ensuring no vouchers are associated with it.
     *
     * @param id ID of the AcChart to delete
     */
    void deleteChart(Long id);
}
