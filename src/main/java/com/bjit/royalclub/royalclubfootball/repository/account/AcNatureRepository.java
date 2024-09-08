package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcNatureRepository extends JpaRepository<AcNature, Long> {
}
