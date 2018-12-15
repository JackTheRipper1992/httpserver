package com.sk.webserver.http.handlers;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.IOException;


public interface Handler {

    /**
     *
     * @param httpRequest
     * @param httpResponse
     * @return status of the respone or 0 if 200
     * @throws IOException
     */
    int execute(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException;

}
