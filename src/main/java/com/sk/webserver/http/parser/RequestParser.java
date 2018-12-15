package com.sk.webserver.http.parser;

import com.sk.webserver.http.request.HttpRequest;

import java.io.IOException;
import java.io.InputStream;

public interface RequestParser {

    HttpRequest parse() throws IOException;
}
