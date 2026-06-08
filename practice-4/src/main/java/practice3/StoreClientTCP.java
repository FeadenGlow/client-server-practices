package practice3;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.PacketMessage;
import practice1.PacketRequest;
import practice2.Processor;

import java.io.IOException;
import java.net.Socket;

public class StoreClientTCP {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private final Encryptor encryptor = new Encryptor();
    private final Decryptor decryptor = new Decryptor();

    private Socket socket;

    public static void main(String[] args) throws InterruptedException {
        StoreClientTCP client = new StoreClientTCP();

        client.sendCommand(Processor.ADD_QUANTITY, "buckwheat;10");
        client.sendCommand(Processor.ADD_QUANTITY, "buckwheat;20");
        client.sendCommand(Processor.REMOVE_QUANTITY, "buckwheat;5");
        client.sendCommand(Processor.GET_QUANTITY, "buckwheat");

        client.close();
    }

    public void sendCommand(int commandType, String text) throws InterruptedException {
        PacketMessage message = new PacketMessage(commandType, 1, text);
        PacketRequest request = new PacketRequest(1, message);
        byte[] packet = encryptor.enty_take(request);

        byte[] response = sendPacket(packet);

        if (response != null) {
            PacketMessage responseMessage = decryptor.readPacket(response).getPacketMessage();
            System.out.println("TCP client response: " + responseMessage.getMessage());
        }
    }

    private byte[] sendPacket(byte[] packet) throws InterruptedException {
        while (true) {
            try {
                connectIfNeeded();

                PacketSocketUtil.writePacket(socket.getOutputStream(), packet);

                return PacketSocketUtil.readPacket(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("TCP server is not available. Reconnect in 1 second...");
                close();
                Thread.sleep(1000);
            }
        }
    }

    private void connectIfNeeded() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket(HOST, PORT);
            System.out.println("TCP client connected to server");
        }
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Cannot close client socket: " + e.getMessage());
        }
    }
}