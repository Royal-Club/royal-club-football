package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;

import java.util.List;

public interface AcVoucherTypeService {

    List<AcVoucherTypeResponse> getAcVoucherTypes();

    AcVoucherType getAcVoucherTypeById(Long id);
}
