package com.sk.webserver.server;

import com.sk.webserver.http.handlers.FileContextHandler;
import com.sk.webserver.http.handlers.Handler;

import java.io.IOException;

public interface Server {

    void start() throws IOException;

    void stop() throws IOException;

    void addContext(String s, Handler handler);
}
