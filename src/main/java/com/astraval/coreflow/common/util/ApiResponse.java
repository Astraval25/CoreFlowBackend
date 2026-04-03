package com.astraval.coreflow.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean responseStatus;        // true if operation is successful
    private int responseCode;               // 200 - ok, 201 Created, 202 Accepted, 203 updated, 204 deleted.
    private String responseMessage;        // human-readable message
    private T responseData; // actual response object (generic)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaginationInfo pagination; // pagination metadata (null for non-paginated responses)

    public ApiResponse(boolean responseStatus, int responseCode, String responseMessage, T responseData) {
        this.responseStatus = responseStatus;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseData = responseData;
        this.pagination = null;
    }
}
