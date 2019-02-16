package io.muserver.sample;

import io.muserver.*;
import io.muserver.handlers.ResourceHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Starting mu-server-sample app");
        MuServer server = MuServerBuilder.muServer()
            .withHttpPort(18080)
            .withHttpsPort(18443)
            .withHttpsConfig(SSLContextBuilder.sslContext()
                    .withKeystoreFromClasspath("/keystore.jks")
                    .withKeystoreType("JKS")
                    .withKeystorePassword("Very5ecure")
                    .withKeyPassword("ActuallyNotSecure")
                    .build()
            )
            .addHandler(new RequestLoggingHandler())
            .addHandler(Method.GET, "/current-time", (request, response, pathParams) -> {
                response.status(200);
                response.contentType(ContentTypes.TEXT_PLAIN);
                response.write(Instant.now().toString());
            })
            .addHandler(ResourceHandlerBuilder.fileOrClasspath("src/main/resources/web", "/web")
                .withPathToServeFrom("/")
                .withDefaultFile("index.html")
                .build())
            .start();

        log.info("Server started at " + server.httpUri() + " and " + server.httpsUri());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.stop();
            log.info("Shut down complete.");
        }));

    }

    private static class RequestLoggingHandler implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) {
            log.info(request.method() + " " + request.uri());
            return false;
        }
    }
}
