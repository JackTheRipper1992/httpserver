package com.sk.webserver.http.parser;

import com.sk.webserver.http.response.Status;

public class ParsingException extends RuntimeException{

    private static final long serialVersionUID = 6569838532917408380L;

    private final Status status;

    public ParsingException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public ParsingException(Status status, String message, Exception e) {
        super(message, e);
        this.status = status;
    }

}
