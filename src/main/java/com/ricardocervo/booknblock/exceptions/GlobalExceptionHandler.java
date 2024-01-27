package com.ricardocervo.booknblock.exceptions;

import com.ricardocervo.booknblock.infra.SecurityService;
import com.ricardocervo.booknblock.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SecurityService securityService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " - "  + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Input field validation has failed", errors);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDetails> handleConflictException(ConflictException ex) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDetails> handleForbiddenException(ForbiddenException ex) {
        return buildResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDetails> handleExpiredJwtException(ExpiredJwtException ex) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    private ResponseEntity<ErrorDetails> buildResponseEntity(HttpStatus status, String message, List<String> details) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setHttpError(status.getReasonPhrase());
        errorDetails.setHttpStatus(status.value());
        errorDetails.setTimestamp(LocalDateTime.now());
        errorDetails.setMessage(message);
        errorDetails.setDetails(details);
        User loggedUser = securityService.getLoggedUser();
        if (loggedUser != null) {
            errorDetails.setLoggedUser(securityService.getLoggedUser().getEmail());
        }
        return ResponseEntity.status(status).body(errorDetails);
    }
}
