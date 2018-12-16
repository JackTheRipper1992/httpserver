package com.sk.webserver.http.request;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestBuilder {


    private URI uri;
    private URL url;

    private HttpMethod method;

    private Map<String, List<String>> params = new HashMap<>();

    private Map<String, String> headers = new HashMap<>();

    //private CookieHandler cookies;

    private String queryParameterString;

    private String remoteIp;

    private String remoteHostname;

    private String protocolVersion;

    public HttpRequestBuilder(){

    }

    public HttpRequest create (){
        return new HttpRequest(
        uri ,
        method ,
        params ,
        headers ,
        queryParameterString ,
        remoteIp ,
        remoteHostname ,
        protocolVersion,
        url
        );

    }
    public HttpRequestBuilder addHeader(final String header,final String value) {
        this.headers.put(header,value);
        return this;
    }

    public HttpRequestBuilder setUri(final URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpRequestBuilder setMethod(final HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder setParms(final Map<String, List<String>> params) {
        this.params = params;
        return this;
    }

    public HttpRequestBuilder setHeaders(final Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder setQueryParameterString(final String queryParameterString) {
        this.queryParameterString = queryParameterString;
        return this;
    }

    public HttpRequestBuilder setRemoteIp(final String remoteIp) {
        this.remoteIp = remoteIp;
        return this;
    }

    public HttpRequestBuilder setRemoteHostname(final String remoteHostname) {
        this.remoteHostname = remoteHostname;
        return this;
    }

    public HttpRequestBuilder setProtocolVersion(final String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public HttpRequestBuilder setURL(final URL url) {
        this.url = url;
        return this;
    }
}
