package practice2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sender {
    private final List<byte[]> sentMessages = Collections.synchronizedList(new ArrayList<>());

    public void sendMessage(byte[] message) {
        sentMessages.add(message);

        System.out.println("Sent encrypted response: " + Arrays.toString(message));
    }

    public List<byte[]> getSentMessages() {
        synchronized (sentMessages) {
            return new ArrayList<>(sentMessages);
        }
    }
}