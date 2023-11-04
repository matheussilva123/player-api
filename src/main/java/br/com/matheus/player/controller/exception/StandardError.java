package br.com.matheus.player.controller.exception;

import java.io.Serializable;

public class StandardError implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer status;
    private String messageError;
    private String timestamp;

    public StandardError(final Integer status, final String messageError, final String timestamp) {
        this.status = status;
        this.messageError = messageError;
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    public String getMessageError() {
        return messageError;
    }

    public void setMessageError(final String messageError) {
        this.messageError = messageError;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }
}

