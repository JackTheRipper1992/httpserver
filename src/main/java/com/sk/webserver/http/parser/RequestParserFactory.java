package com.sk.webserver.http.parser;

import java.net.Socket;

public class RequestParserFactory {

    public static RequestParser getParser(final Socket socket) {
        return new HttpRequestParser(socket);
    }
}
