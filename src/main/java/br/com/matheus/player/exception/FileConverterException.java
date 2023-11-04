package br.com.matheus.player.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FileConverterException extends RuntimeException {

    public FileConverterException(final String message) {
        super(message);
    }

}
