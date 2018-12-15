package com.sk.webserver.main;

import com.sk.webserver.http.handlers.FileContextHandler;
import com.sk.webserver.http.handlers.Handler;
import com.sk.webserver.http.handlers.HealthCheckRequestHandler;
import com.sk.webserver.http.request.HttpRequest;
import com.sk.webserver.http.response.HttpResponse;
import com.sk.webserver.server.Server;
import com.sk.webserver.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            if (args.length == 0) {
                System.err.printf("Usage: java [-options] %s <directory> [port]%n" + Main.class.getName());
                return;
            }
            File dir = new File(args[0]);
            Server server = new HttpServer(8080);
            server.addContext("/",new FileContextHandler(dir));
            server.addContext("/getServerInfo", (httpRequest, httpResponse) -> {
                long now = System.currentTimeMillis();
                httpResponse.getHeaders().put("Content-Type", "text/plain");
                httpResponse.send(200, String.format("This is test server. The time is %tF %<tT", now));
                return 0;
            });
            server.addContext("/health",new HealthCheckRequestHandler());
            server.start();

        } catch (IOException e) {
            logger.error("*** Error starting http server",e);
        }
    }
}
