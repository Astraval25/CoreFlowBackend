package com.astraval.coreflow.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean responseStatus;        // true if operation is successful
    private int responseCode;               // 200 - ok, 201 Created, 202 Accepted, 203 updated, 204 deleted.
    private String responseMessage;        // human-readable message
    private T responseData;                // actual response object (generic)
    private Map<String, Object> optional;  // optional extra fields

}
