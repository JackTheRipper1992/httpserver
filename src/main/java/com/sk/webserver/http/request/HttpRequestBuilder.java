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

    private Map<String, List<String>> parms = new HashMap<>();

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
        parms ,
        headers ,
        queryParameterString ,
        remoteIp ,
        remoteHostname ,
        protocolVersion,
        url
        );

    }
    public HttpRequestBuilder addHeader(String header,String value) {
        this.headers.put(header,value);
        return this;
    }

    public HttpRequestBuilder setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public HttpRequestBuilder setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder setParms(Map<String, List<String>> parms) {
        this.parms = parms;
        return this;
    }

    public HttpRequestBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder setQueryParameterString(String queryParameterString) {
        this.queryParameterString = queryParameterString;
        return this;
    }

    public HttpRequestBuilder setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
        return this;
    }

    public HttpRequestBuilder setRemoteHostname(String remoteHostname) {
        this.remoteHostname = remoteHostname;
        return this;
    }

    public HttpRequestBuilder setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public HttpRequestBuilder setURL(final URL url) {
        this.url = url;
        return this;
    }
}
