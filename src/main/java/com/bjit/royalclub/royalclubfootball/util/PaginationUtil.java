package com.bjit.royalclub.royalclubfootball.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    private static final String DEFAULT_SORTED_COLUMN = "id";

    private PaginationUtil() {
    }

    public static Pageable createPageable(int offSet, int pageSize, String sortedBy, String sortDirection) {
        int pageNo = offSet >= 0 ? offSet : DEFAULT_PAGE_NUMBER;
        int currentPerPage = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        String sortColumn = sortedBy != null ? sortedBy : DEFAULT_SORTED_COLUMN;
        String sortDir = (sortDirection != null && sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()))
                ? Sort.Direction.ASC.name()
                : Sort.Direction.DESC.name();

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortColumn).ascending()
                : Sort.by(sortColumn).descending();

        return PageRequest.of(pageNo, currentPerPage, sort);
    }
}
