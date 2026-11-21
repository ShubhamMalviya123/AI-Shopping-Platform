package com.kharid.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Request failed." : e.getReason()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> rejected(HttpClientErrorException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "The AI service could not process that input. For image search, upload a PNG or JPG."));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> mlProblem(RestClientException e) {
        String message = e instanceof ResourceAccessException
                ? "The AI service is not running. Start ml-service on port 8000."
                : "The AI service returned an error. Check the ml-service terminal.";
        return ResponseEntity.status(503).body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).orElse("Please check your input.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(413).body(Map.of("message", "That image is too large (limit 8 MB)."));
    }
}
