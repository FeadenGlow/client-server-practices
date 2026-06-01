package practice3;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.PacketMessage;
import practice1.PacketRequest;
import practice2.Processor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class StoreClientUDP {
    private static final String HOST = "localhost";
    private static final int PORT = 5001;

    private static final int BUFFER_SIZE = 10_000;
    private static final int TIMEOUT_MS = 1000;
    private static final int MAX_ATTEMPTS = 3;

    private final Encryptor encryptor = new Encryptor();
    private final Decryptor decryptor = new Decryptor();

    public static void main(String[] args) {
        StoreClientUDP client = new StoreClientUDP();

        client.sendCommand(Processor.ADD_QUANTITY, "buckwheat;10");
        client.sendCommand(Processor.ADD_QUANTITY, "buckwheat;20");
        client.sendCommand(Processor.REMOVE_QUANTITY, "buckwheat;5");
        client.sendCommand(Processor.GET_QUANTITY, "buckwheat");
    }

    public void sendCommand(int commandType, String text) {
        PacketMessage message = new PacketMessage(commandType, 1, text);
        PacketRequest request = new PacketRequest(1, message);
        byte[] packet = encryptor.enty_take(request);

        byte[] response = sendPacket(packet);

        if (response != null) {
            PacketMessage responseMessage = decryptor.readPacket(response).getPacketMessage();
            System.out.println("UDP client response: " + responseMessage.getMessage());
        }
    }

    private byte[] sendPacket(byte[] packet) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            InetAddress address = InetAddress.getByName(HOST);

            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                DatagramPacket requestPacket = new DatagramPacket(
                        packet,
                        packet.length,
                        address,
                        PORT
                );

                socket.send(requestPacket);

                System.out.println("UDP client sent packet, attempt " + attempt);

                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

                    socket.receive(responsePacket);

                    byte[] response = new byte[responsePacket.getLength()];
                    System.arraycopy(
                            responsePacket.getData(),
                            0,
                            response,
                            0,
                            responsePacket.getLength()
                    );

                    return response;
                } catch (SocketTimeoutException e) {
                    System.out.println("UDP response timeout");
                }
            }
        } catch (IOException e) {
            System.out.println("UDP client error: " + e.getMessage());
        }

        System.out.println("UDP server did not respond");

        return null;
    }
}