import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChatServer - run this on ONE computer in the LAN.
 * It accepts many clients and forwards every message to everyone.
 */
public class ChatServer {

    private static final int PORT = 5000;
    // list of all connected clients (thread-safe)
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Chat server started on port " + PORT);
        System.out.println("Server IP address(es) to give to clients:");
        printLocalIPs();

        while (true) {
            Socket socket = serverSocket.accept();      // wait for a client
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();                // one thread per client
        }
    }

    // send a message to all clients
    static void broadcast(String message) {
        System.out.println(message);
        for (ClientHandler c : clients) {
            c.send(message);
        }
    }

    static void remove(ClientHandler c) {
        clients.remove(c);
    }

    static String onlineUsers() {
        StringBuilder sb = new StringBuilder();
        for (ClientHandler c : clients) sb.append(c.name).append(", ");
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "none";
    }

    static void printLocalIPs() throws SocketException {
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                if (a instanceof Inet4Address && !a.isLoopbackAddress())
                    System.out.println("   -> " + a.getHostAddress());
            }
        }
    }

    // ---------- handles one client ----------
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        String name = "Unknown";

        ClientHandler(Socket socket) { this.socket = socket; }

        void send(String msg) {
            if (out != null) out.println(msg);
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                name = in.readLine();                    // first line = username
                broadcast(">> " + name + " joined the chat");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("/users")) {
                        send(">> Online: " + onlineUsers());
                    } else {
                        broadcast(name + ": " + line);
                    }
                }
            } catch (IOException e) {
                // client disconnected
            } finally {
                remove(this);
                broadcast(">> " + name + " left the chat");
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
