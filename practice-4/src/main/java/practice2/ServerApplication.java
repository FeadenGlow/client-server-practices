package practice2;

import practice3.NetworkPacket;
import practice4.ProductService;

public class ServerApplication {
    private final SharedQueue<NetworkPacket> packetsFromNetwork = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToDecrypt = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToProcess = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToEncrypt = new SharedQueue<>(100);
    private final SharedQueue<NetworkPacket> packetsToSend = new SharedQueue<>(100);

    private final Receiver receiver;
    private final DecryptorWorker decryptorWorker;
    private final Processor processor;
    private final EncryptorWorker encryptorWorker;
    private final Sender sender;

    private final Thread receiverThread;
    private final Thread decryptorThread;
    private final Thread processorThread;
    private final Thread encryptorThread;
    private final Thread senderThread;

    public ServerApplication() {
        this(new ProductService());
    }

    public ServerApplication(ProductService productService) {
        receiver = new Receiver(packetsFromNetwork, packetsToDecrypt);
        decryptorWorker = new DecryptorWorker(packetsToDecrypt, packetsToProcess);
        processor = new Processor(packetsToProcess, packetsToEncrypt, productService);
        encryptorWorker = new EncryptorWorker(packetsToEncrypt, packetsToSend);
        sender = new Sender(packetsToSend);

        receiverThread = new Thread(receiver, "Receiver");
        decryptorThread = new Thread(decryptorWorker, "Decryptor");
        processorThread = new Thread(processor, "Processor");
        encryptorThread = new Thread(encryptorWorker, "Encryptor");
        senderThread = new Thread(sender, "Sender");
    }

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