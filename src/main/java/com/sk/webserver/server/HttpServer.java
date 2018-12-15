package com.sk.webserver.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.worker.WorkerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

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

    @Override
    public void addContext(String path, Handler handler) {
        path = trimRight(path, '/'); // remove trailing slash
        this.getContextMap().put(path,handler);
    }
}
