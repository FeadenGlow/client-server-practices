package practice2;

import practice1.Encryptor;
import practice1.PacketRequest;

public class EncryptorWorker implements Runnable {
    private final SharedQueue<PacketRequest> responsesToEncrypt;
    private final SharedQueue<byte[]> responsesToSend;
    private final Encryptor encryptor = new Encryptor();

    public EncryptorWorker(
            SharedQueue<PacketRequest> responsesToEncrypt,
            SharedQueue<byte[]> responsesToSend
    ) {
        this.responsesToEncrypt = responsesToEncrypt;
        this.responsesToSend = responsesToSend;
    }

    @Override
    public void run() {
        try {
            PacketRequest response;

            while ((response = responsesToEncrypt.take()) != null) {
                byte[] encryptedResponse = encryptor.enty_take(response);

                responsesToSend.put(encryptedResponse);

                System.out.println("Encryptor: encrypted response");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            responsesToSend.close();
        }
    }
}