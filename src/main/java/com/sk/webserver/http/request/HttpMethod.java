package com.sk.webserver.http.request;

public enum HttpMethod {

    GET,
    HEAD,
    OPTIONS,
    TRACE;

    public static HttpMethod lookup(final String method) {
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
