package practice2;

import practice1.PacketMessage;
import practice1.PacketRequest;

import java.util.HashMap;
import java.util.Map;

public class Processor implements Runnable {
    public static final int GET_QUANTITY = 1;
    public static final int ADD_QUANTITY = 2;
    public static final int REMOVE_QUANTITY = 3;

    private static final int SERVER_SOURCE = 0;

    private final Map<String, Integer> products = new HashMap<>();

    private final SharedQueue<PacketMessage> messagesToProcess;
    private final SharedQueue<PacketRequest> responsesToEncrypt;

    public Processor(
            SharedQueue<PacketMessage> messagesToProcess,
            SharedQueue<PacketRequest> responsesToEncrypt
    ) {
        this.messagesToProcess = messagesToProcess;
        this.responsesToEncrypt = responsesToEncrypt;
    }

    @Override
    public void run() {
        try {
            PacketMessage message;

            while ((message = messagesToProcess.take()) != null) {
                PacketMessage responseMessage = process(message);
                PacketRequest responseRequest = new PacketRequest(SERVER_SOURCE, responseMessage);

                responsesToEncrypt.put(responseRequest);

                System.out.println("Processor: processed message");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            responsesToEncrypt.close();
        }
    }

    public synchronized PacketMessage process(PacketMessage message) {
        if (message.getCommandType() == GET_QUANTITY) {
            return getQuantity(message);
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

    private PacketMessage getQuantity(PacketMessage message) {
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