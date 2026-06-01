package practice2;

import practice3.NetworkPacket;

public class ServerApplication {
    private final SharedQueue<NetworkPacket> packetsFromNetwork = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToDecrypt = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToProcess = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToEncrypt = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToSend = new SharedQueue<>(100);

    private final Receiver receiver = new Receiver(packetsFromNetwork, packetsToDecrypt);
    private final DecryptorWorker decryptorWorker = new DecryptorWorker(packetsToDecrypt, packetsToProcess);
    private final Processor processor = new Processor(packetsToProcess, packetsToEncrypt);
    private final EncryptorWorker encryptorWorker = new EncryptorWorker(packetsToEncrypt, packetsToSend);
    private final Sender sender = new Sender(packetsToSend);

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

    public void receivePacket(NetworkPacket packet) throws InterruptedException {
        receiver.receive(packet);
    }

    public void receivePacket(byte[] data) throws InterruptedException {
        receiver.receive(NetworkPacket.fake(data));
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