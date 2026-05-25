package practice2;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.PacketData;
import practice1.PacketMessage;
import practice1.PacketRequest;

public class ServerApplication {
    private static final int SERVER_SOURCE = 0;

    private final SharedQueue<byte[]> incomingMessages = new SharedQueue<>(100);

    private final SharedQueue<byte[]> receivedPackets = new SharedQueue<>(100);

    private final SharedQueue<PacketMessage> decryptedMessages = new SharedQueue<>(100);

    private final SharedQueue<PacketRequest> responses = new SharedQueue<>(100);

    private final SharedQueue<byte[]> encryptedResponses = new SharedQueue<>(100);

    private final Receiver receiver = new Receiver(incomingMessages);
    private final Decryptor decryptor = new Decryptor();
    private final Processor processor = new Processor();
    private final Encryptor encryptor = new Encryptor();
    private final Sender sender = new Sender();

    private final Thread receiverThread;
    private final Thread decryptorThread;
    private final Thread processorThread;
    private final Thread encryptorThread;
    private final Thread senderThread;

    public ServerApplication() {
        receiverThread = new Thread(() -> {
            try {
                byte[] message;

                while ((message = receiver.receiveMessage()) != null) {
                    receivedPackets.produce(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                receivedPackets.close();
            }
        }, "Receiver");

        decryptorThread = new Thread(() -> {
            try {
                byte[] packet;

                while ((packet = receivedPackets.consume()) != null) {
                    PacketData packetData = decryptor.readPacket(packet);

                    decryptedMessages.produce(packetData.getPacketMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                decryptedMessages.close();
            }
        }, "Decryptor");

        processorThread = new Thread(() -> {
            try {
                PacketMessage message;

                while ((message = decryptedMessages.consume()) != null) {
                    PacketMessage response = processor.process(message);

                    responses.produce(new PacketRequest(SERVER_SOURCE, response));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                responses.close();
            }
        }, "Processor");

        encryptorThread = new Thread(() -> {
            try {
                PacketRequest response;

                while ((response = responses.consume()) != null) {
                    byte[] encryptedResponse = encryptor.enty_take(response);

                    encryptedResponses.produce(encryptedResponse);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                encryptedResponses.close();
            }
        }, "Encryptor");

        senderThread = new Thread(() -> {
            try {
                byte[] message;

                while ((message = encryptedResponses.consume()) != null) {
                    sender.sendMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Sender");
    }

    public void start() {
        receiverThread.start();
        decryptorThread.start();
        processorThread.start();
        encryptorThread.start();
        senderThread.start();
    }

    public void send(byte[] message) throws InterruptedException {
        receiver.addMessage(message);
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