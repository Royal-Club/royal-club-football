package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.ResourceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceAttachmentRepository extends JpaRepository<ResourceAttachment, Long> {

    List<ResourceAttachment> findByResourceIdOrderBySortOrderAscIdAsc(Long resourceId);
}
