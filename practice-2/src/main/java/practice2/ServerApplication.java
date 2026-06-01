package practice2;

import practice1.PacketMessage;
import practice1.PacketRequest;

public class ServerApplication {
    private final SharedQueue<byte[]> messagesFromClient = new SharedQueue<>(100);
    private final SharedQueue<byte[]> packetsToDecrypt = new SharedQueue<>(100);
    private final SharedQueue<PacketMessage> messagesToProcess = new SharedQueue<>(100);
    private final SharedQueue<PacketRequest> responsesToEncrypt = new SharedQueue<>(100);
    private final SharedQueue<byte[]> responsesToSend = new SharedQueue<>(100);

    private final Receiver receiver = new Receiver(messagesFromClient, packetsToDecrypt);
    private final DecryptorWorker decryptorWorker = new DecryptorWorker(packetsToDecrypt, messagesToProcess);
    private final Processor processor = new Processor(messagesToProcess, responsesToEncrypt);
    private final EncryptorWorker encryptorWorker = new EncryptorWorker(responsesToEncrypt, responsesToSend);
    private final Sender sender = new Sender(responsesToSend);

    private final Thread receiverThread = new Thread(receiver, "Receiver");
    private final Thread decryptorThread = new Thread(decryptorWorker, "Decryptor");
    private final Thread processorThread = new Thread(processor, "Processor");
    private final Thread encryptorThread = new Thread(encryptorWorker, "Encryptor");
    private final Thread senderThread = new Thread(sender, "Sender");

    public void start() {
        receiverThread.start();
        decryptorThread.start();
        processorThread.start();
        encryptorThread.start();
        senderThread.start();
    }

    public void receivePacket(byte[] message) throws InterruptedException {
        receiver.receive(message);
    }

    public void finishSending() {
        receiver.close();
    }

    public void waitUntilFinished() throws InterruptedException {
        receiverThread.join();
        decryptorThread.join();
        processorThread.join();
        encryptorThread.join();
        senderThread.join();
    }

    public Processor getProcessor() {
        return processor;
    }

    public Sender getSender() {
        return sender;
    }
}