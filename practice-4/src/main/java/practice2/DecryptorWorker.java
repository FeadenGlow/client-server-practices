package practice2;

import practice1.Decryptor;
import practice1.PacketData;
import practice3.NetworkPacket;

public class DecryptorWorker implements Runnable {
    private final SharedQueue<NetworkPacket> packetsToDecrypt;
    private final SharedQueue<NetworkPacket> packetsToProcess;
    private final Decryptor decryptor = new Decryptor();

    public DecryptorWorker(
            SharedQueue<NetworkPacket> packetsToDecrypt,
            SharedQueue<NetworkPacket> packetsToProcess
    ) {
        this.packetsToDecrypt = packetsToDecrypt;
        this.packetsToProcess = packetsToProcess;
    }

    @Override
    public void run() {
        try {
            NetworkPacket packet;

            while ((packet = packetsToDecrypt.take()) != null) {
                PacketData packetData = decryptor.readPacket(packet.getData());

                packet.setPacketData(packetData);
                packetsToProcess.put(packet);

                System.out.println("Decryptor: decrypted packet");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            packetsToProcess.close();
        }
    }
}