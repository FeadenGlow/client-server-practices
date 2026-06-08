package practice1;

public class Main {

    public static void main(String[] args) {
        Encryptor encryptor = new Encryptor();

        PacketMessage message = new PacketMessage(1, 1, "{\"text\":\"Hello from client\"}");

        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        System.out.println("Packet bytes:");
        System.out.println(Utils.toHex(packet));

        Decryptor decryptor = new Decryptor();
        PacketData data = decryptor.readPacket(packet);

        System.out.println();
        System.out.println("Parsed packet:");
        System.out.println(data);
    }
}