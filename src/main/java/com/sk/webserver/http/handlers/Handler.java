package com.sk.webserver.http.handlers;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.IOException;


public interface Handler {

    int execute(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException;

}
