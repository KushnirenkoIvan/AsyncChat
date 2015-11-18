package kushnirenko.ivan.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

public class Server {

    private ArrayList<ClientHandler> clients;
    private ServerSocketChannel serverSocketChannel;
    String gettedMessage = new String();

    public String getGettedMessage() {
        return gettedMessage;
    }

    public void setGettedMessage(String gettedMessage) {
        this.gettedMessage = gettedMessage;
    }

    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public void setClients(ArrayList<ClientHandler> clients) {
        this.clients = clients;
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public void setServerSocketChannel(ServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }

    public Server(int port, String ipAddress) {
        clients = new ArrayList<ClientHandler>();
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
        } catch (UnknownHostException exp) {
            System.out.println("ERROR: Unknown host.");
            exp.printStackTrace();
        } catch (IOException exp) {
            System.out.println("ERROR: Cannot create server.");
            exp.printStackTrace();
        }
        System.out.println("Server is created.");
    }

    public void getConnections() {
        Thread connector = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        synchronized (clients) {
                            clients.add(new ClientHandler(i, serverSocketChannel.accept(), Server.this));
                        }
                    } catch (IOException exp) {
                        System.out.println("ERROR: Cannot connect client.");
                        exp.printStackTrace();
                    }
                    i++;
                }
            }
        };
        connector.start();
    }

    public void sendMessage(String message) {
        ArrayList<ClientHandler> clients = this.clients;
        for (ClientHandler clh : clients) {
            if (!clh.isHaveMessage()) {
                clh.sendMessage(message);
            } else {
                clh.setHaveMessage(false);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(30001, "localhost");
        server.getConnections();
    }
}
