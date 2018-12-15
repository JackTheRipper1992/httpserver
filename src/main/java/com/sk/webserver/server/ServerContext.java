package com.sk.webserver.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class ServerContext {

    /**
     * contextMap holds uri path to http method and handler mapping.
     * For eg :
     *  OPTIONS /*
     *  will have entry in contextMap as follows:
     *  /* -> OPTIONS,OptionsHandler
     */
    private Map<String,Map<HttpMethod,Handler>> contextMap = new HashMap<>();

    ServerContext(final Map<String,Map<HttpMethod,Handler>> contextMap){
        this.contextMap = contextMap;
    }

    public Map<String,Map<HttpMethod,Handler>> getContextMap() {
        return contextMap;
    }
}
