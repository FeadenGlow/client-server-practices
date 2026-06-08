package practice2;

import practice1.Encryptor;
import practice3.NetworkPacket;

public class EncryptorWorker implements Runnable {
    private final SharedQueue<NetworkPacket> packetsToEncrypt;
    private final SharedQueue<NetworkPacket> packetsToSend;
    private final Encryptor encryptor = new Encryptor();

    public EncryptorWorker(
            SharedQueue<NetworkPacket> packetsToEncrypt,
            SharedQueue<NetworkPacket> packetsToSend
    ) {
        this.packetsToEncrypt = packetsToEncrypt;
        this.packetsToSend = packetsToSend;
    }

    @Override
    public void run() {
        try {
            NetworkPacket packet;

            while ((packet = packetsToEncrypt.take()) != null) {
                byte[] encryptedResponse = encryptor.enty_take(packet.getResponseRequest());

                packet.setData(encryptedResponse);
                packetsToSend.put(packet);

                System.out.println("Encryptor: encrypted response");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            packetsToSend.close();
        }
    }
}