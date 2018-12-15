package com.sk.webserver.http.validator;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;

import java.io.IOException;

public interface Validator {

    boolean validate(final HttpRequest httpRequest,
                     final HttpResponse httpResponse) throws IOException;
}
