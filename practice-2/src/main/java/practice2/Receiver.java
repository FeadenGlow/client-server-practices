package practice2;

public class Receiver {
    private final SharedQueue<byte[]> incomingMessages;

    public Receiver(SharedQueue<byte[]> incomingMessages) {
        this.incomingMessages = incomingMessages;
    }

    public void addMessage(byte[] message) throws InterruptedException {
        incomingMessages.produce(message);
    }

    public byte[] receiveMessage() throws InterruptedException {
        return incomingMessages.consume();
    }

    public void close() {
        incomingMessages.close();
    }
}