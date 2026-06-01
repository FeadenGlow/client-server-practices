package practice3;

import practice2.ServerApplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StoreServerTCP {
    private static final int PORT = 5000;

    private final ServerApplication serverApplication = new ServerApplication();

    public static void main(String[] args) {
        StoreServerTCP server = new StoreServerTCP();
        server.start();
    }

    public void start() {
        serverApplication.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("TCP client connected: " + clientSocket.getRemoteSocketAddress());

                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("TCP server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            while (true) {
                byte[] packet = PacketSocketUtil.readPacket(clientSocket.getInputStream());

                if (packet == null) {
                    break;
                }

                serverApplication.receivePacket(NetworkPacket.tcp(packet, clientSocket));
            }
        } catch (IOException e) {
            System.out.println("TCP client disconnected: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            closeSocket(clientSocket);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Cannot close socket: " + e.getMessage());
        }
    }
}