package com.sk.webserver.server;

import com.sk.webserver.http.handlers.Handler;

import java.util.HashMap;
import java.util.Map;

public class ServerContext {

    private Map<String,Handler> contextMap = new HashMap<>();

    ServerContext(final Map<String,Handler> contextMap){
        this.contextMap = contextMap;
    }

    public Map<String, Handler> getContextMap() {
        return contextMap;
    }
}
