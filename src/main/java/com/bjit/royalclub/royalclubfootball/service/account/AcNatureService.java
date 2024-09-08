package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcNature;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;

import java.util.List;

public interface AcNatureService {

    List<AcNatureResponse> getAcNatures();

    AcNatureResponse convertToResponse(AcNature entity);

}
