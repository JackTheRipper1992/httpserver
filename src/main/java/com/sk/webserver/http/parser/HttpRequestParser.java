package com.sk.webserver.http.parser;

import com.sk.webserver.http.HttpMethod;
import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.request.HttpRequestBuilder;
import com.sk.webserver.http.response.Status;
import com.sk.webserver.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class HttpRequestParser implements RequestParser {

    public static final int BUFFER = 8192;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class.getName());

    private final Socket socket;
    private final HttpRequestBuilder httpRequestBuilder;

    public HttpRequestParser(Socket socket) {
        this.socket = socket;
        this.httpRequestBuilder = new HttpRequestBuilder();
    }

    @Override
    public HttpRequest parse() throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        createHeaderAndParams(httpRequestBuilder,in);

        return httpRequestBuilder.create();
    }

    private void createHeaderAndParams(HttpRequestBuilder httpRequestBuilder, BufferedReader in) throws IOException {


        // read first line of request header
        readFirstLine(in,httpRequestBuilder);

        String line = in.readLine();
        while (line != null && !line.trim().isEmpty()) {
            int p = line.indexOf(':');
            if (p >= 0) {
                httpRequestBuilder.addHeader(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
            }
            line = in.readLine();
        }

        final InetAddress inetAddress = this.socket.getInetAddress();
        if (null != inetAddress.getHostAddress()) {
            httpRequestBuilder.addHeader("remote-addr", inetAddress.getHostAddress());
            httpRequestBuilder.addHeader("http-client-ip", inetAddress.getHostAddress());
        }
    }

    private void readFirstLine(BufferedReader in, HttpRequestBuilder httpRequestBuilder) throws IOException {
        String requestLine;
        try {
            do {
                requestLine = in.readLine();
            } while (requestLine.length() == 0);
        } catch (IOException ioe) { // if EOF, timeout etc.
            throw new IOException("Request Line Missing");
        }

        if (requestLine == null) {
            return ;
        }

        logger.info(requestLine);

        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3)
            throw new IOException("invalid request line: \"" + requestLine + "\"");

        try {
            String method = tokens[0];
            httpRequestBuilder.addHeader("method", method);
            httpRequestBuilder.setMethod(HttpMethod.valueOf(method));
            // must remove '//' prefix which constructor parses as host name
            String uriToken = tokens[1];
            URI uri = new URI(uriToken);
            httpRequestBuilder.setUri(uri);
            String protocolVersion = tokens[2]; // RFC2616#2.1: allow implied LWS; RFC7230#3.1.1: disallow it
            httpRequestBuilder.setProtocolVersion(protocolVersion);
        } catch (URISyntaxException use) {
            throw new IOException("invalid URI: " + use.getMessage());
        }
    }

    private void readParams(String parms, Map<String, List<String>> params) {

        if (parms == null) {
            httpRequestBuilder.setQueryParameterString("");
            return;
        }

        httpRequestBuilder.setQueryParameterString(parms);
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String key;
            String value;

            if (sep >= 0) {
                key = HttpUtils.decodePercent(e.substring(0, sep)).trim();
                value = HttpUtils.decodePercent(e.substring(sep + 1));
            } else {
                key = HttpUtils.decodePercent(e).trim();
                value = "";
            }

            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                params.put(key, values);
            }

            values.add(value);
        }
    }

}
