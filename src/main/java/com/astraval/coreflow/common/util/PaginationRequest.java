package com.astraval.coreflow.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

@Data
public class PaginationRequest {
    private int page = 0;
    private int size = 10;
    private String search;
    private String sortBy = "sort_date";
    private String sortDirection = "desc";

    public Pageable toPageable() {
        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
