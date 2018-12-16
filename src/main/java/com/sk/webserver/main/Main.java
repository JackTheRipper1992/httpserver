package com.sk.webserver.main;

import com.sk.webserver.http.handlers.FileContextHandler;
import com.sk.webserver.http.handlers.HealthCheckRequestHandler;
import com.sk.webserver.http.request.HttpMethod;
import com.sk.webserver.http.server.Server;
import com.sk.webserver.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            if (args.length < 2) {
                System.err.printf("Usage: java [-options] %s <directory> [port] [true|false for healthCheckEnabled]"
                        , Main.class.getName());
                return;
            }
            File dir = new File(args[0]);
            String portStr = args[1];
            boolean isHealthCheckEnabled = false;
            if(args.length == 3){
                isHealthCheckEnabled = Boolean.valueOf(args[2]);
            }
            int port = Integer.parseInt(portStr);
            Server server = new HttpServer(port);
            server.addHandler(HttpMethod.GET,"/",new FileContextHandler(dir));
            server.addHandler(HttpMethod.GET,"/getServerInfo", (httpRequest, httpResponse) -> {
                long now = System.currentTimeMillis();
                httpResponse.getHeaders().put("Content-Type", "text/plain");
                httpResponse.send(200, String.format("This is test server. The time is %tF %<tT", now));
                return 0;
            });
            if(isHealthCheckEnabled)
                server.isHealthCheckEnabled(isHealthCheckEnabled);

            server.addHandler(HttpMethod.GET,"/health",new HealthCheckRequestHandler());
            server.start();

        } catch (IOException e) {
            logger.error("*** Error starting http server",e);
        }
    }
}
