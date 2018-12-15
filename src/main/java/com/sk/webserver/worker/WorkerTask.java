package com.sk.webserver.worker;

import com.sk.webserver.http.handlers.FileContextHandler;
import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpMethod;
import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.parser.RequestParser;
import com.sk.webserver.http.parser.RequestParserFactory;
import com.sk.webserver.http.response.HttpResponse;
import com.sk.webserver.http.validator.Validator;
import com.sk.webserver.server.ServerContext;
import com.sk.webserver.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.sk.webserver.utils.HttpUtils.getParentPath;
import static com.sk.webserver.utils.HttpUtils.trimRight;
import static java.lang.String.join;

public class WorkerTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(WorkerTask.class.getName());

    private final ServerContext serverContext;
    private final Socket socket;
    private InputStream in;
    private OutputStream out;
    //private final Validator validator;

    public WorkerTask(final Socket socket,
                      final ServerContext serverContext) {
        this.socket = socket;
        this.serverContext = serverContext;
    }

    @Override
    public void run() throws IOException {

        logger.info("Received task from socket {}", socket.getInetAddress().getHostName());

        HttpRequest httpRequest = null;
        HttpResponse httpResponse = null;
        out = socket.getOutputStream();
        in = socket.getInputStream();

        do {
            try {
                RequestParser parser = RequestParserFactory.getParser(socket); //todo if we have different parsers

                httpRequest = parser.parse();
                logger.info("{}", httpRequest);
                httpResponse = new HttpResponse(out, httpRequest);

                //if(validateHttpRequest(httpRequest,httpResponse))
                handleRequest(httpRequest, httpResponse); //handle the request

            }catch(Throwable throwable){
                logger.error("Something really went wrong :" ,throwable);
                if (httpRequest == null) { // error reading request
                    if (throwable instanceof IOException && throwable.getMessage().contains("Request Line Missing"))
                        break; // proceed to close connection - just disconnect
                    httpResponse.getHeaders().put("Connection", "close"); // about to close connection
                    if (throwable instanceof InterruptedIOException) // e.g. SocketTimeoutException
                        httpResponse.sendError(408, "Timeout waiting for client request");
                    else
                        httpResponse.sendError(400, "Invalid request: " + throwable.getMessage());
                } else if (!httpResponse.isResponseFlushed()) { // if headers were not already sent, we can send an error response
                    httpResponse = new HttpResponse(out,httpRequest); // ignore whatever headers may have already been set
                    httpResponse.getHeaders().put("Connection", "close"); // about to close connection
                    httpResponse.sendError(500, "Error processing request: " + throwable.getMessage());
                } // otherwise just abort the connection since we can't recover
                break; // proceed to close connection
            }
            finally {
                httpResponse.close(); // close response and flush output
            }
        }while (!"close".equalsIgnoreCase(httpRequest.getHeaders().get("Connection"))         //Persistent Connection HTTP 1.1
                && !"close".equalsIgnoreCase(httpResponse.getHeaders().get("Connection")) && httpRequest.getProtocolVersion().endsWith("1.1"));

    }

    private void handleRequest(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Handler handler = getContext(httpRequest.getPath(),httpRequest.getMethod());
        if(handler != null) {

            handler.execute(httpRequest, httpResponse);


            /*    if (httpRequest.getPath().equals("/*")) {
                    Set<String> methods = new LinkedHashSet<>();
                    methods.addAll(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS")); // built-in methods
                    httpResponse.getHeaders().put("Allow", join(", ", methods));
                    httpResponse.getHeaders().put("Content-Length", "0");
                    httpResponse.sendHeaders(200);
                }*/
        } else
            httpResponse.sendError(501); // unsupported method

    }

    public Handler getContext(String path,
                              final HttpMethod httpMethod) {
        // all context paths are without trailing slash
        Map<String, Map<HttpMethod,Handler>> contextMap = serverContext.getContextMap();
        for (path = trimRight(path, '/'); path != null; path = getParentPath(path)) {
            Map<HttpMethod,Handler> handlerByMethod = contextMap.get(path);
            if(handlerByMethod != null && !handlerByMethod.isEmpty()) {
                Handler handler = handlerByMethod.get(httpMethod);
                if (handler != null)
                    return handler;
            }
        }
        return contextMap.get("").get(HttpMethod.GET); //return default handler i.e. FileContextHandler
    }

}