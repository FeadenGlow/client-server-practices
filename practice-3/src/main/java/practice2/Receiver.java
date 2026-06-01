package practice2;
import practice3.NetworkPacket;

public class Receiver implements Runnable {
    private final SharedQueue<NetworkPacket> packetsFromNetwork;
    private final SharedQueue<NetworkPacket> packetsToDecrypt;

    public Receiver(
            SharedQueue<NetworkPacket> packetsFromNetwork,
            SharedQueue<NetworkPacket> packetsToDecrypt
    ) {
        this.packetsFromNetwork = packetsFromNetwork;
        this.packetsToDecrypt = packetsToDecrypt;
    }

    public void receive(NetworkPacket packet) throws InterruptedException {
        packetsFromNetwork.put(packet);
    }

    public void close() {
        packetsFromNetwork.close();
    }

    @Override
    public void run() {
        try {
            NetworkPacket packet;

            while ((packet = packetsFromNetwork.take()) != null) {
                packetsToDecrypt.put(packet);
                System.out.println("Receiver: received packet");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            packetsToDecrypt.close();
        }
    }
}