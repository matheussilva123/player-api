package br.com.matheus.player.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FileUploadException extends RuntimeException {

    public FileUploadException(final String message) {
        super(message);
    }

}
