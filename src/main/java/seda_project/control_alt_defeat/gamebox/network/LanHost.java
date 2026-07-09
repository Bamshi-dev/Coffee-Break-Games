package seda_project.control_alt_defeat.gamebox.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public final class LanHost {

    private LanHost() {}

    public static NetworkLayer host(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(0); // wait forever for a client
            Socket client = server.accept(); // blocks until client connects and then reutrns the socket
            return new LanSession(client); // wrap in lansession and return as NetworkLayer
        }
    }

    /**
     * Handle for a pending (not yet accepted) host attempt. close() cancels it:
     * the server socket is closed so the accept thread unblocks and the port is
     * released immediately. Without this, backing out of a lobby left a daemon
     * thread parked in accept() holding the port forever, so the next attempt
     * to host ANY game failed with "Address already in use: bind".
     */
    public static final class Pending implements AutoCloseable {
        private volatile ServerSocket server;
        private volatile boolean cancelled;

        @Override
        public void close() {
            cancelled = true;
            ServerSocket s = server;
            if (s != null) {
                try { s.close(); } catch (IOException ignored) {}
            }
        }
    }

    // async wrapper to prevent blocking ux so we run it in the background and use cb
    public static Pending hostAsync(int port,
                                    Consumer<NetworkLayer> onReady, // called with NetworkLayer once client is connected
                                    Consumer<Throwable>    onError) { // called when anything goes wrong
        Pending pending = new Pending();
        Thread t = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                pending.server = server;
                if (pending.cancelled) return; // cancelled while the socket was being created
                server.setSoTimeout(0); // wait forever for a client
                Socket client = server.accept(); // blocks until a client connects
                NetworkLayer layer = new LanSession(client);
                if (pending.cancelled) {
                    // cancel raced the accept: drop the connection instead of
                    // delivering it to a screen the user already left
                    try { layer.close(); } catch (Exception ignored) {}
                    return;
                }
                onReady.accept(layer);
            } catch (Throwable e) {
                if (!pending.cancelled) onError.accept(e); // closing on cancel throws; not a real error
            }
        }, "lan-host-accept");
        t.setDaemon(true);
        t.start();
        return pending;
    }
}