package practice2;

import practice1.PacketData;
import practice1.PacketMessage;
import practice1.PacketRequest;
import practice3.NetworkPacket;

import java.util.HashMap;
import java.util.Map;

public class Processor implements Runnable {
    public static final int GET_QUANTITY = 1;
    public static final int ADD_QUANTITY = 2;
    public static final int REMOVE_QUANTITY = 3;

    private static final int SERVER_SOURCE = 0;

    private final Map<String, Integer> products = new HashMap<>();
    private final Map<String, PacketRequest> udpProcessedPackets = new HashMap<>();

    private final SharedQueue<NetworkPacket> packetsToProcess;
    private final SharedQueue<NetworkPacket> packetsToEncrypt;

    public Processor(
            SharedQueue<NetworkPacket> packetsToProcess,
            SharedQueue<NetworkPacket> packetsToEncrypt
    ) {
        this.packetsToProcess = packetsToProcess;
        this.packetsToEncrypt = packetsToEncrypt;
    }

    @Override
    public void run() {
        try {
            NetworkPacket packet;

            while ((packet = packetsToProcess.take()) != null) {
                PacketRequest responseRequest = createResponse(packet);

                packet.setResponseRequest(responseRequest);
                packetsToEncrypt.put(packet);

                System.out.println("Processor: processed message");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            packetsToEncrypt.close();
        }
    }

    private synchronized PacketRequest createResponse(NetworkPacket packet) {
        if (packet.isUdp()) {
            String packetKey = createUdpPacketKey(packet);

            if (udpProcessedPackets.containsKey(packetKey)) {
                return udpProcessedPackets.get(packetKey);
            }

            PacketMessage responseMessage = process(packet.getPacketData().getPacketMessage());
            PacketRequest responseRequest = new PacketRequest(SERVER_SOURCE, responseMessage);

            udpProcessedPackets.put(packetKey, responseRequest);

            return responseRequest;
        }

        PacketMessage responseMessage = process(packet.getPacketData().getPacketMessage());

        return new PacketRequest(SERVER_SOURCE, responseMessage);
    }

    private String createUdpPacketKey(NetworkPacket packet) {
        PacketData packetData = packet.getPacketData();

        return packet.getClientKey() + ":" +
                packetData.getSource() + ":" +
                packetData.getPacketId();
    }

    public synchronized PacketMessage process(PacketMessage message) {
        if (message.getCommandType() == GET_QUANTITY) {
            return getQuantityResponse(message);
        }

        String[] data = message.getMessage().split(";");

        if (data.length != 2) {
            return response(message, "ERROR;wrong message format");
        }

        String productName = data[0];
        int amount = parseAmount(data[1]);

        if (amount <= 0) {
            return response(message, "ERROR;wrong amount");
        }

        if (message.getCommandType() == ADD_QUANTITY) {
            return addQuantity(message, productName, amount);
        }

        if (message.getCommandType() == REMOVE_QUANTITY) {
            return removeQuantity(message, productName, amount);
        }

        return response(message, "ERROR;unknown command");
    }

    private PacketMessage getQuantityResponse(PacketMessage message) {
        String productName = message.getMessage();
        int quantity = products.getOrDefault(productName, 0);

        return response(message, "OK;quantity=" + quantity);
    }

    private PacketMessage addQuantity(PacketMessage message, String productName, int amount) {
        int currentQuantity = products.getOrDefault(productName, 0);
        int newQuantity = currentQuantity + amount;

        products.put(productName, newQuantity);

        return response(message, "OK;quantity=" + newQuantity);
    }

    private PacketMessage removeQuantity(PacketMessage message, String productName, int amount) {
        int currentQuantity = products.getOrDefault(productName, 0);

        if (currentQuantity < amount) {
            return response(message, "ERROR;not enough products");
        }

        int newQuantity = currentQuantity - amount;

        products.put(productName, newQuantity);

        return response(message, "OK;quantity=" + newQuantity);
    }

    private int parseAmount(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public synchronized int getQuantity(String productName) {
        return products.getOrDefault(productName, 0);
    }

    private PacketMessage response(PacketMessage request, String text) {
        return new PacketMessage(
                request.getCommandType(),
                request.getUserId(),
                text
        );
    }
}