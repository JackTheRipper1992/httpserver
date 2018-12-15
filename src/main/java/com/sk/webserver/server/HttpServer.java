package com.sk.webserver.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpMethod;
import com.sk.webserver.worker.WorkerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.sk.webserver.utils.HttpUtils.trimRight;


public class HttpServer extends AbstractHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class.getName());

    public HttpServer(int port) throws IOException {
        super(port);
    }


    @Override
    protected void processRequest(final Socket clientSocket,
                                  final ServerContext serverContext) {
        logger.info("Processing HTTP request from address",clientSocket.getInetAddress());
        try {
            try {
                new WorkerTask(clientSocket,serverContext).run();
            } finally {
                logger.info("Closing the socket {}", clientSocket.getInetAddress().getHostName());
                clientSocket.close(); // and finally close socket fully
            }
        } catch (IOException ignore) {}

    }

    /**
     * Adds the handler for given path and http method to the existing context of the
     * server.
     * @param httpMethod
     * @param path
     * @param handler
     */
    @Override
    public void addHandler(HttpMethod httpMethod, String path, Handler handler) {
        path = trimRight(path, '/');
        Map<HttpMethod, Handler> handlerByHttpMethodMap = this.getContextMap().getOrDefault(path,new HashMap<>());
        handlerByHttpMethodMap.put(httpMethod,handler);
        this.getContextMap().put(path,handlerByHttpMethodMap);
    }
}
