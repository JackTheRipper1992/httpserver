package com.sk.webserver.http.request;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    protected URI uri;
    protected URL url;
    protected String path;
    protected HttpMethod method;
    protected Map<String, List<String>> parms;
    protected Map<String, String> headers;
    protected String queryParameterString;
    protected String remoteIp;
    protected String remoteHostname;
    protected String protocolVersion;

    public HttpRequest(final URI uri,
                       final HttpMethod method,
                       final Map<String, List<String>> parms,
                       final Map<String, String> headers,
                       final String queryParameterString,
                       final String remoteIp,
                       final String remoteHostname,
                       final String protocolVersion,
                       final URL url) {
        this.uri = uri;
        this.method = method;
        this.parms = parms;
        this.headers = headers;
        this.queryParameterString = queryParameterString;
        this.remoteIp = remoteIp;
        this.remoteHostname = remoteHostname;
        this.protocolVersion = protocolVersion;
        this.url = url;
    }

    public URI getUri() {
        return uri;
    }

    public String getPath() {
        return this.uri.getPath();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, List<String>> getParms() {
        return parms;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getQueryParameterString() {
        return queryParameterString;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "uri='" + uri + '\'' +
                ", method=" + method +
                ", parms=" + parms +
                ", headers=" + headers +
                ", queryParameterString='" + queryParameterString + '\'' +
                ", remoteIp='" + remoteIp + '\'' +
                ", remoteHostname='" + remoteHostname + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                '}';
    }
}
