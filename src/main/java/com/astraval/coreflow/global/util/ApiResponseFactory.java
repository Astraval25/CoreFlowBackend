package com.astraval.coreflow.global.util;

public class ApiResponseFactory {

    public static <T> ApiResponse<T> ok(T data, String message){
        return new ApiResponse<>(true, 200, message, data, null);
    }

    public static <T> ApiResponse<T> created(T data, String message){
        return new ApiResponse<>(true, 201, message, data, null);
    }

    public static <T> ApiResponse<T> accepted(T data, String message){
        return new ApiResponse<>(true, 202, message, data, null);
    }

    public static <T> ApiResponse<T> updated(T data, String message){
        return new ApiResponse<>(true, 203, message, data, null);
    }

    public static <T> ApiResponse<T> deleted( String message){
        return new ApiResponse<>(true, 204, message, null, null);
    }
    public static <T> ApiResponse<T> softDelete( String message){
        return new ApiResponse<>(true, 200, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, int code){
        return new ApiResponse<>(false, code, message, null, null);
    }

    public static <T> ApiResponse<T> UnauthorizedAccess(String message){
        return new ApiResponse<>(false, 401, message, null, null);
    }
    
    public static <T> ApiResponse<T> badRequest(String message){
        return new ApiResponse<>(false, 400, message, null, null);
    }
    
}
