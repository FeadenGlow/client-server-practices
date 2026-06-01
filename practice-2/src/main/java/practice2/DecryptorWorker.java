package practice2;

import practice1.Decryptor;
import practice1.PacketData;
import practice1.PacketMessage;

public class DecryptorWorker implements Runnable {
    private final SharedQueue<byte[]> packetsToDecrypt;
    private final SharedQueue<PacketMessage> messagesToProcess;
    private final Decryptor decryptor = new Decryptor();

    public DecryptorWorker(
            SharedQueue<byte[]> packetsToDecrypt,
            SharedQueue<PacketMessage> messagesToProcess
    ) {
        this.packetsToDecrypt = packetsToDecrypt;
        this.messagesToProcess = messagesToProcess;
    }

    @Override
    public void run() {
        try {
            byte[] packet;

            while ((packet = packetsToDecrypt.take()) != null) {
                PacketData packetData = decryptor.readPacket(packet);
                PacketMessage message = packetData.getPacketMessage();

                messagesToProcess.put(message);

                System.out.println("Decryptor: decrypted packet");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            messagesToProcess.close();
        }
    }
}