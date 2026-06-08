package practice3;

import practice2.ServerApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class StoreServerUDP {
    private static final int PORT = 5001;
    private static final int BUFFER_SIZE = 10_000;

    private final ServerApplication serverApplication = new ServerApplication();

    public static void main(String[] args) {
        StoreServerUDP server = new StoreServerUDP();
        server.start();
    }

    public void start() {
        serverApplication.start();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("UDP server started on port " + PORT);

            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

                socket.receive(datagramPacket);

                byte[] packetBytes = Arrays.copyOf(
                        datagramPacket.getData(),
                        datagramPacket.getLength()
                );

                NetworkPacket packet = NetworkPacket.udp(
                        packetBytes,
                        socket,
                        datagramPacket.getAddress(),
                        datagramPacket.getPort()
                );

                serverApplication.receivePacket(packet);

                System.out.println("UDP server received packet");
            }
        } catch (IOException e) {
            System.out.println("UDP server error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}