package com.danielflower.ronin.muserver.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ronin.muserver.*;
import ronin.muserver.handlers.ResourceHandler;

import java.time.Instant;

public class App {
    public static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Starting mu-server-sample app");
        MuServer server = MuServerBuilder.muServer()
                .withHttpConnection(8080)
                .withHttpsConnection(8443, SSLContextBuilder.unsignedLocalhostCert())
                .addHandler(new LoggingHandler())
                .addHandler(Method.GET, "/current-time", (request, response) -> {
                    response.headers().set(HeaderNames.CONTENT_TYPE, ContentTypes.TEXT_PLAIN);
                    response.write(Instant.now().toString());
                    return true;
                })
                .addHandler(ResourceHandler.resourceHandler("src/main/resources/web", "/web")
                        .withPathToServeFrom("/")
                        .withDefaultFile("index.html")
                        .build())
                .start();
        log.info("Server started at " + server.uri() + " and " + server.httpsUri());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Shutting down...");
                server.stop();
                log.info("Shut down complete.");
            }
        }));

    }

    private static class LoggingHandler implements MuHandler {
        @Override
        public boolean handle(MuRequest request, MuResponse response) throws Exception {
            log.info(request.method() + " " + request.uri());
            return false;
        }
    }
}
