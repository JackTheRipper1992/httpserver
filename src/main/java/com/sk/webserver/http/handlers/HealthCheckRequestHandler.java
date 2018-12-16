package com.sk.webserver.http.handlers;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.IOException;

public class HealthCheckRequestHandler implements Handler{

    private static final int status = 200;
    private static final String text = "OK";

    @Override
    public int execute(final HttpRequest httpRequest,
                       final HttpResponse httpResponse) throws IOException {
        httpResponse.getHeaders().put("Content-Type", "text/plain");
        httpResponse.send(status, text);
        return 0;
    }
}
