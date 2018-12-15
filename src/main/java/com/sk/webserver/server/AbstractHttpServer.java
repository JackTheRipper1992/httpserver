package com.sk.webserver.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHttpServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpServer.class.getName());

    private final int port;
    private final int socketTimeout = 50000;
    private ServerSocket serverSocket;
    private Map<String,Handler> contextMap = new HashMap<>();
    private ServerContext serverContext;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public AbstractHttpServer(int port) throws IOException {
        this.port = port;
    }

    public void start() throws IOException {
        if (serverSocket != null)
            return;
        logger.info("*** Starting Http Server ***");

        serverContext = new ServerContext(contextMap);
        createServerSocket();

        getSingleThreadExecutor().submit(() -> {
            //todo ServerSocketChannel
            while(serverSocket!=null && !serverSocket.isClosed()){
                try {
                    final Socket clientSocket = serverSocket.accept();
                    getExecutorService().submit(() -> {
                        try {
                            try {
                                clientSocket.setSoTimeout(socketTimeout);
                                processRequest(clientSocket,serverContext);
                            } catch (SocketException e) {
                                e.printStackTrace();
                            }
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    logger.error("Error accepting the client connection",e);

                }
            }
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                createHealthCheckRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }

        },1000, 2000,TimeUnit.MILLISECONDS);
        logger.info("*** Http Server started successfully on http://{}:{} !***",serverSocket.getInetAddress().getCanonicalHostName(),serverSocket.getLocalPort());

    }

    /**
     * Health check.
     * //todo Close server socket if any issue.
     * @throws IOException
     */
    private void createHealthCheckRequest() throws IOException {
        String urlStr = "http://localhost:8080/health";
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        logger.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    private void createServerSocket() throws IOException {
        this.serverSocket = ServerSocketFactory.getDefault().createServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
    }

    @Override
    public void stop() throws IOException {

        logger.info("*** Trying to stop Http Server ***");
        serverSocket.close();
        if(isOpen(serverSocket)){
            serverSocket.close();
            logger.info("*** Http Server stopped successfully ***");
        }
    }

    protected abstract void processRequest(Socket clientSocket, ServerContext serverContext);

    public static boolean isOpen(ServerSocket ss) {
        return ss.isBound() && ! ss.isClosed();
    }
    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ExecutorService getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public String getHostName(){
        return serverSocket.getInetAddress().getHostName();
    }

    public Map<String, Handler> getContextMap() {
        return contextMap;
    }

    public ServerContext getServerContext(){
        return serverContext;
    }
}
