package com.example.backendeventmanagementbooking.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    @JsonIgnore
    private HttpStatus status;

    private String message;

    private T data;

    public GenericResponse(HttpStatus status) {
        this.status = status;
        this.message = status.getReasonPhrase();
    }

    public GenericResponse(HttpStatus status, T data) {
        this.status = status;
        this.data = data;
        this.message = status.getReasonPhrase();
    }

    public GenericResponse(String message, HttpStatus status) {
        this.status = status;
        this.message = message;
    }

    public static <T> ResponseEntity<GenericResponse<T>> GenerateResponse(HttpStatus status) {
        return ResponseEntity.status(status).body(new GenericResponse<>(status));
    }

    public static <T> ResponseEntity<GenericResponse<T>> GenerateResponse(HttpStatus status, T data) {
        return ResponseEntity.status(status).body(new GenericResponse<>(status, status.getReasonPhrase(), data));
    }

    public static <T> ResponseEntity<GenericResponse<T>> GenerateResponse(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status).body(new GenericResponse<>(status, message, data));
    }

    public ResponseEntity<GenericResponse<T>> GenerateResponse() {
        return GenerateResponse(this.status, this.message, this.data);
    }

}
