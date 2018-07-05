package test.ronin.muserver.sample;

import io.netty.util.ResourceLeakDetector;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ronin.muserver.MuServer;
import ronin.muserver.sample.App;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AppTest {

    private MuServer server;

    @AfterEach void tearDown() {
        server.stop();
    }

    @Test void hitTheSample() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        server = App.mu_server_builder.start();
        // need to be able to override the http / https port here to do tests.
        // Https is a bit fiddly requiring port + sslContext

        // feels as though all builders should be built during start()

        OkHttpClient client = newClient().build();

        Request request = new Request.Builder().url(server.httpsUri().resolve("/index.html").toURL()).build();

        Response result = client.newCall(request).execute();

        // assert something interesting here

        result.close();
    }

    public static OkHttpClient.Builder newClient() throws KeyManagementException, NoSuchAlgorithmException {
        X509TrustManager trusting = new X509TrustManager() {
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }

            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
        };

        return new OkHttpClient.Builder()
            .hostnameVerifier((hostname, session) -> true)
            .sslSocketFactory(sslSocketFactory(trusting), trusting);
    }

    private static SSLSocketFactory sslSocketFactory(X509TrustManager trust) throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{trust}, null);
        return context.getSocketFactory();
    }
}
