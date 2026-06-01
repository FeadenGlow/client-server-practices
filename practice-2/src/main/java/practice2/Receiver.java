package practice2;

public class Receiver implements Runnable {
    private final SharedQueue<byte[]> messagesFromClient;
    private final SharedQueue<byte[]> packetsToDecrypt;

    public Receiver(
            SharedQueue<byte[]> messagesFromClient,
            SharedQueue<byte[]> packetsToDecrypt
    ) {
        this.messagesFromClient = messagesFromClient;
        this.packetsToDecrypt = packetsToDecrypt;
    }

    public void receive(byte[] message) throws InterruptedException {
        messagesFromClient.put(message);
    }

    public void close() {
        messagesFromClient.close();
    }

    @Override
    public void run() {
        try {
            byte[] message;

            while ((message = messagesFromClient.take()) != null) {
                packetsToDecrypt.put(message);
                System.out.println("Receiver: received packet");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            packetsToDecrypt.close();
        }
    }
}