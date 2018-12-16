package com.sk.webserver.http.response;

import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.utils.HttpUtils;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


import static com.sk.webserver.http.response.HttpResponseUtils.statuses;

public class HttpResponse implements Closeable {

    private final OutputStream out;
    private final HttpRequest httpRequest;
    private Map<String, String> headers = new HashMap<>();
    private boolean responseFlushed;


    public HttpResponse(final OutputStream out, final HttpRequest httpRequest) {
        this.out = out;
        this.httpRequest = httpRequest;
    }

    public OutputStream getResponseOutputStream() {
        return out;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Sends an error response with the given status and detailed message.
     * An HTML body is created containing the status and its description,
     * as well as the message.
     *
     * @param status the response status
     * @param text   the text body (sent as text/html)
     * @throws IOException if an error occurs
     */
    public void sendError(final int status, final String text) throws IOException {
        send(status, String.format(
                "<!DOCTYPE html>%n<html>%n<head><title>%d %s</title></head>%n" +
                        "<body><h1>%d %s</h1>%n<p>%s</p>%n</body></html>",
                status, statuses[status], status, statuses[status], escapeHtml4(text)));
    }

    public void sendError(final int status) throws IOException {
        String text = status < 400 ? ":)" : "sorry it didn't work out :(";
        sendError(status, text);
    }

    /**
     * Sends the response with the given status, and the given string
     * as the body.
     *
     * @param status the response status
     * @param text   the text body (sent as text/html)
     * @throws IOException if an error occurs
     */
    public void send(final int status, final String text) throws IOException {
        byte[] content = text.getBytes("UTF-8");
        sendHeaders(status, content.length, -1,
                "W/\"" + Integer.toHexString(text.hashCode()) + "\"",
                "text/html; charset=utf-8");
        if (out != null)
            out.write(content);
    }

    /**
     * Sends the response headers, including the given response status
     * and description, and all response headers.
     * @param status       the response status
     * @param length       the response body length
     * @param lastModified the last modified date of the response resource,
     * @param etag         the ETag of the response resource
     * @param contentType  the content type of the response resource, or null
     * @throws IOException if an error occurs
     */
    public void sendHeaders(final int status,
                            final long length,
                            final long lastModified,
                            final String etag,
                            final String contentType) throws IOException {

        String ct = headers.get("Content-Type");
        if (ct == null) {
            ct = contentType != null ? contentType : "application/octet-stream";
            addHeader("Content-Type", ct);
        }
        if (length >= 0) {
            addHeader("Content-Length", Long.toString(length)); // known length
        }
        if (lastModified > 0 && !headers.containsKey("Last-Modified")) {// RFC2616#14.29
            addHeader("Last-Modified", HttpUtils.getServerTime());
        }
        if (etag != null && !headers.containsKey("ETag"))
            addHeader("ETag", etag);
        if (httpRequest != null && "close".equalsIgnoreCase(httpRequest.getHeaders().get("Connection"))
                && !headers.containsKey("Connection"))
            addHeader("Connection", "close"); // #RFC7230#6.6: should reply to close with close
        sendHeaders(status);
    }


    /**
     * Sends the response headers with the given response status.
     * A Date header is added if it does not already exist.
     *
     * @param status the response status
     * @throws IOException if an error occurs or headers were already sent
     * @see #sendHeaders(int, long, long, String, String)
     */
    public void sendHeaders(final int status) throws IOException {

        if (!headers.containsKey("Date"))
            headers.put("Date", HttpUtils.getServerTime());
        headers.put("Server", "TestServer");
        out.write(HttpUtils.getBytes("HTTP/1.1 ", Integer.toString(status), " ", statuses[status]));
        out.write(HttpUtils.getBytes("\n"));
        writeTo(out);
        responseFlushed = true; // response header written to client socket

    }

    public void sendBody(final InputStream body, final long length) throws IOException {
        if (out != null) {
            writeResponse(body, out, length);
        }
    }

    public static void writeResponse(final InputStream in,
                                     final OutputStream out,
                                     long len) throws IOException {
        if (len == 0 || out == null && len < 0 && in.read() < 0)
            return; // small optimization - avoid buffer creation
        byte[] buf = new byte[4096];
        while (len != 0) {
            int count = len < 0 || buf.length < len ? buf.length : (int)len;
            count = in.read(buf, 0, count);
            if (count < 0) {
                if (len > 0)
                    throw new IOException("unexpected end of stream");
                break;
            }
            if (out != null)
                out.write(buf, 0, count);
            len -= len > 0 ? count : 0;
        }
    }

    public void addHeader(final String header, final String value) {
        this.headers.put(header, value);
    }

    public void removeHeader(final String header) {
        this.headers.remove(header);
    }

    public void addAll(final Map<String, String> headers) {
        for (Map.Entry<String, String> headerEntry : headers.entrySet())
            this.headers.put(headerEntry.getKey(), headerEntry.getValue());
    }

    public String replace(final String name, final String value) {
        return headers.put(name, value);

    }


    public void writeTo(final OutputStream out) throws IOException {
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            out.write(HttpUtils.getBytes(headerEntry.getKey(), ": ", headerEntry.getValue()));
            out.write(HttpUtils.getBytes("\n"));
        }
        out.write(HttpUtils.getBytes("\n"));
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return true if response has already been written to output stream of
     * client socket else false
     */
    public boolean isResponseFlushed() {
        return responseFlushed;
    }

    public void redirect(String url) throws IOException {
        try {
            url = new URI(url).toASCIIString();
        } catch (URISyntaxException e) {
            throw new IOException("malformed URL: " + url);
        }
        headers.put("Location", url);

            sendError(301, "Permanently moved to " + url);
    }
}

