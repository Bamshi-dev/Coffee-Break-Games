package seda_project.control_alt_defeat.gamebox.network;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanSessionTest {

    private static final int TEST_PORT = 18765;

    @Test
    void roundtripHello() throws Exception {
        AtomicReference<Message> hostReceived   = new AtomicReference<>();
        AtomicReference<Message> clientReceived = new AtomicReference<>();
        CountDownLatch hostGot   = new CountDownLatch(1);
        CountDownLatch clientGot = new CountDownLatch(1);

        AtomicReference<NetworkLayer> hostLayer = new AtomicReference<>();
        Thread hostThread = new Thread(() -> {
            try {
                NetworkLayer host = LanHost.host(TEST_PORT);
                hostLayer.set(host);
                host.addListener(new NetworkListener() {
                    @Override public void onMessage(Message msg) {
                        hostReceived.set(msg);
                        hostGot.countDown();
                    }
                });
                host.send(new GameMessage.Hello("HostPlayer"));
                Thread.sleep(2000);
            } catch (Exception e) { e.printStackTrace(); }
        });
        hostThread.setDaemon(true);
        hostThread.start();

        Thread.sleep(250); // give the server a moment to bind

        NetworkLayer client = LanClient.join("127.0.0.1", TEST_PORT);
        client.addListener(new NetworkListener() {
            @Override public void onMessage(Message msg) {
                clientReceived.set(msg);
                clientGot.countDown();
            }
        });
        client.send(new GameMessage.Hello("ClientPlayer"));

        assertTrue(hostGot.await(2, TimeUnit.SECONDS),   "host did not receive client hello");
        assertTrue(clientGot.await(2, TimeUnit.SECONDS), "client did not receive host hello");
        assertInstanceOf(GameMessage.Hello.class, hostReceived.get());
        assertInstanceOf(GameMessage.Hello.class, clientReceived.get());

        client.close();
        if (hostLayer.get() != null) hostLayer.get().close();
    }

    @Test
    void disconnectFiresWhenPeerCloses() throws Exception {
        int port = TEST_PORT + 1;
        CountDownLatch disconnected = new CountDownLatch(1);

        AtomicReference<NetworkLayer> hostLayer = new AtomicReference<>();
        Thread hostThread = new Thread(() -> {
            try {
                NetworkLayer host = LanHost.host(port);
                hostLayer.set(host);
                host.addListener(new NetworkListener() {
                    @Override public void onDisconnected(String reason) {
                        disconnected.countDown();
                    }
                });
                Thread.sleep(8000);
            } catch (Exception e) { e.printStackTrace(); }
        });
        hostThread.setDaemon(true);
        hostThread.start();

        Thread.sleep(250);

        NetworkLayer client = LanClient.join("127.0.0.1", port);
        Thread.sleep(250);
        client.close();

        assertTrue(disconnected.await(7, TimeUnit.SECONDS),
                "host did not detect client disconnect within 7s");

        if (hostLayer.get() != null) hostLayer.get().close();
    }

    @Test
    void cancelledHostReleasesThePort() throws Exception {
        // Regression test: hosting a lobby and backing out before anyone joins
        // must free the port, otherwise hosting the next game (any game — they
        // all share DEFAULT_PORT) fails with "Address already in use: bind".
        int port = TEST_PORT + 2;

        LanHost.Pending first = LanHost.hostAsync(port,
                layer -> {}, err -> {});
        Thread.sleep(250);   // let it bind and block in accept()
        first.close();       // simulate pressing Back in the lobby
        Thread.sleep(250);   // let the accept thread unwind

        // Hosting again on the same port must now succeed.
        CountDownLatch secondReady = new CountDownLatch(1);
        AtomicReference<Throwable> secondError = new AtomicReference<>();
        AtomicReference<NetworkLayer> hostLayer = new AtomicReference<>();
        LanHost.Pending second = LanHost.hostAsync(port,
                layer -> { hostLayer.set(layer); secondReady.countDown(); },
                err   -> { secondError.set(err); secondReady.countDown(); });
        Thread.sleep(250);

        NetworkLayer client = LanClient.join("127.0.0.1", port);
        assertTrue(secondReady.await(2, TimeUnit.SECONDS), "second host attempt did not accept");
        if (secondError.get() != null) {
            throw new AssertionError("second host attempt failed: " + secondError.get(), secondError.get());
        }

        client.close();
        if (hostLayer.get() != null) hostLayer.get().close();
        second.close();
    }
}