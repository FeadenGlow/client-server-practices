package practice2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sender implements Runnable {
    private final SharedQueue<byte[]> responsesToSend;
    private final List<byte[]> sentMessages = Collections.synchronizedList(new ArrayList<>());

    public Sender(SharedQueue<byte[]> responsesToSend) {
        this.responsesToSend = responsesToSend;
    }

    @Override
    public void run() {
        try {
            byte[] message;

            while ((message = responsesToSend.take()) != null) {
                sendMessage(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendMessage(byte[] message) {
        sentMessages.add(message);

        System.out.println("Sender: sent encrypted response " + Arrays.toString(message));
    }

    public List<byte[]> getSentMessages() {
        synchronized (sentMessages) {
            return new ArrayList<>(sentMessages);
        }
    }
}