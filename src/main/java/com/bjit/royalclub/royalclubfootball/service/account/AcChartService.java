package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;

import java.util.List;

public interface AcChartService {

    List<AcChartResponse> getAcCharts();

    AcChart getAcChartById(Long id);

    AcChartResponse getAcChartResponse(AcChart acChart);

}
