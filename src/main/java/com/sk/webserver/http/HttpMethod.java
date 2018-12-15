package com.sk.webserver.http;

public enum HttpMethod {

    GET,
    PUT,
    POST,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE;

    public static HttpMethod lookup(String method) {
        if (method == null)
            return null;

        try {
            return valueOf(method);
        } catch (IllegalArgumentException e) {
            // TODO: Log it?
            return null;
        }
    }
}
