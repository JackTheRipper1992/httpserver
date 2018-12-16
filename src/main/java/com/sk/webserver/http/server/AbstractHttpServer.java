package com.sk.webserver.http.server;

import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.request.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.String.join;

public abstract class AbstractHttpServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpServer.class.getName());

    protected final int port;
    protected final int socketTimeout = 50000;
    protected boolean isHealthCheckEnabled;
    protected ServerSocket serverSocket;
    protected Map<String,Map<HttpMethod,Handler>> contextMap = new HashMap<>(); //path -> Method,Handler mapping
    protected ServerContext serverContext;

    protected ExecutorService executorService = Executors.newCachedThreadPool();
    protected ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    protected final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public AbstractHttpServer(int port) throws IOException {
        this.port = port;
    }

    public void start() throws IOException {
        if (serverSocket != null)
            return;
        logger.info("*** Starting Http Server ***");

        /**
         * If the Request-URI is an asterisk ("*"), the OPTIONS request is
         intended to apply to the server in general rather than to a specific
         resource. Since a server's communication options typically depend on
         the resource, the "*" request is only useful as a "ping" or "no-op"
         type of method; it does nothing beyond allowing the client to test
         the capabilities of the server.
         */
        Map<HttpMethod, Handler> optionsHandlerMap = new HashMap<>();
        optionsHandlerMap.put(HttpMethod.OPTIONS, (httpRequest, httpResponse) -> {
            HttpMethod method = httpRequest.getMethod();
            if(method.equals(HttpMethod.OPTIONS)) {
                Set<String> methods = new LinkedHashSet<>();
                methods.addAll(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS")); // built-in methods
                httpResponse.getHeaders().put("Allow", join(", ", methods));
                httpResponse.getHeaders().put("Content-Length", "0");
                httpResponse.sendHeaders(200);
            }else {
                httpResponse.sendError(501); // unsupported method
            }
            return 0;
        });

        contextMap.put("/*", optionsHandlerMap);

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

        if(isHealthCheckEnabled) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    createHealthCheckRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }, 1000, 2000, TimeUnit.MILLISECONDS);
        }
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

    public Map<String,Map<HttpMethod,Handler>> getContextMap() {
        return contextMap;
    }

    public ServerContext getServerContext(){
        return serverContext;
    }

    @Override
    public void isHealthCheckEnabled(boolean isHealthCheckEnabled) {
        this.isHealthCheckEnabled = isHealthCheckEnabled;
    }

}
