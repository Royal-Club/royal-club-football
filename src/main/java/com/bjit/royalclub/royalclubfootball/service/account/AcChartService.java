package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;

import java.util.List;

public interface AcChartService {

    List<AcChartResponse> getAcCharts();

}
