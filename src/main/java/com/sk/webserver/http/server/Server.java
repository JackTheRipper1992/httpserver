package com.sk.webserver.http.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpMethod;

import java.io.IOException;

public interface Server {

    void start() throws IOException;

    void stop() throws IOException;

    void addHandler(HttpMethod httpMethod, String path, Handler handler);

    void isHealthCheckEnabled(boolean isHealthCheckEnabled);
}
