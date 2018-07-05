package ronin.muserver.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ronin.muserver.ContentTypes;
import ronin.muserver.Method;
import ronin.muserver.MuHandler;
import ronin.muserver.MuRequest;
import ronin.muserver.MuServer;
import ronin.muserver.MuServerBuilder;
import ronin.muserver.SSLContextBuilder;
import ronin.muserver.handlers.ResourceHandler;

import java.time.Instant;
import java.util.function.Function;

import static ronin.muserver.handlers.ResourceHandler.fileOrClasspath;
import static ronin.muserver.sample.Decoration.decorate;

public class App {
    public static final Logger log = LoggerFactory.getLogger(App.class);

    public static final MuServerBuilder mu_server_builder = MuServerBuilder.muServer()
        .withHttpConnection(8080)
        .withHttpsConnection(8443,
            SSLContextBuilder.sslContext()
                .withKeystoreFromClasspath("/keystore.jks")
                .withKeystoreType("JKS")
                .withKeystorePassword("Very5ecure")
                .withKeyPassword("ActuallyNotSecure")
                .build()
        )
        .addHandler(decorate((req, res) -> log.info(req.method() + " " + req.uri())))
        .addHandler(plain_old_get("/current-time", req -> Instant.now().toString()))
        .addHandler(maven_resources("/web")
            .withPathToServeFrom("/")
            .withDefaultFile("index.html")
            .build());

    public static void main(String[] args) {
        log.info("Starting mu-server-sample app");
        MuServer server = mu_server_builder.start();

        log.info("Server started at " + server.httpUri() + " and " + server.httpsUri());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.stop();
            log.info("Shut down complete.");
        }));
    }

    private static ResourceHandler.Builder maven_resources(String spath) {
        return fileOrClasspath("src/main/resources" + spath, spath);
    }

    // possibly put in Routes ?
    private static MuHandler plain_old_get(String path, Function<MuRequest, String> f) {
        return (req, res) -> {
            if (Method.GET.equals(req.method()) && req.uri().getPath().matches(path)) {
                res.status(200);
                res.contentType(ContentTypes.TEXT_PLAIN);
                res.write(f.apply(req));
                return true;
            }
            return false;
        };
    }
}
