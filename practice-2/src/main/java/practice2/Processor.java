package practice2;

import practice1.PacketMessage;

import java.util.HashMap;
import java.util.Map;

public class Processor {
    public static final int GET_QUANTITY = 1;
    public static final int ADD_QUANTITY = 2;
    public static final int REMOVE_QUANTITY = 3;

    private final Map<String, Integer> products = new HashMap<>();

    public synchronized PacketMessage process(PacketMessage message) {
        if (message.getCommandType() == GET_QUANTITY) {
            int quantity = products.getOrDefault(message.getMessage(), 0);
            return response(message, "OK;quantity=" + quantity);
        }

        String[] data = message.getMessage().split(";");

        if (data.length != 2) {
            return response(message, "ERROR;wrong message format");
        }

        String productName = data[0];
        int amount;

        try {
            amount = Integer.parseInt(data[1]);
        } catch (NumberFormatException e) {
            return response(message, "ERROR;wrong amount");
        }

        if (amount < 0) {
            return response(message, "ERROR;wrong amount");
        }

        int currentQuantity = products.getOrDefault(productName, 0);

        if (message.getCommandType() == ADD_QUANTITY) {
            int newQuantity = currentQuantity + amount;
            products.put(productName, newQuantity);

            return response(message, "OK;quantity=" + newQuantity);
        }

        if (message.getCommandType() == REMOVE_QUANTITY) {
            if (currentQuantity < amount) {
                return response(message, "ERROR;not enough products");
            }

            int newQuantity = currentQuantity - amount;
            products.put(productName, newQuantity);

            return response(message, "OK;quantity=" + newQuantity);
        }

        return response(message, "ERROR;unknown command");
    }

    public synchronized int getQuantity(String productName) {
        return products.getOrDefault(productName, 0);
    }

    private PacketMessage response(PacketMessage request, String text) {
        return new PacketMessage(request.getCommandType(), request.getUserId(), text);
    }
}