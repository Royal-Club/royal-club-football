package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TournamentSpecification {

    public Specification<Tournament> hasValue(String column, String value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(column), "%" + value + "%");
    }

    public Specification<Tournament> hasDate(String column, LocalDateTime value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(column), value);
    }
}
