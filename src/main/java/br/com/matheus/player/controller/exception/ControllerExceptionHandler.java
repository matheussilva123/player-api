package br.com.matheus.player.controller.exception;

import br.com.matheus.player.exception.FileConverterException;
import br.com.matheus.player.exception.FileUploadException;
import br.com.matheus.player.exception.ObjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<StandardError> fileUploadException(final FileUploadException fileUploadException, final HttpServletRequest request) {
        StandardError standardError = new StandardError(HttpStatus.BAD_REQUEST.value(), fileUploadException.getMessage(), LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standardError);
    }

    @ExceptionHandler(FileConverterException.class)
    public ResponseEntity<StandardError> fileConverterException(final FileConverterException fileConverterException, final HttpServletRequest request) {
        StandardError standardError = new StandardError(HttpStatus.BAD_REQUEST.value(), fileConverterException.getMessage(), LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standardError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> illegalArgumentException(final IllegalArgumentException fileConverterException, final HttpServletRequest request) {
        StandardError standardError = new StandardError(HttpStatus.BAD_REQUEST.value(), fileConverterException.getMessage(), LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standardError);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> objectNotFoundException(final ObjectNotFoundException objectNotFoundException, final HttpServletRequest request) {
        StandardError standardError = new StandardError(HttpStatus.NOT_FOUND.value(), objectNotFoundException.getMessage(), LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
    }
}
