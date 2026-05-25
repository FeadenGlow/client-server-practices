package practice2;

import practice1.Encryptor;
import practice1.PacketMessage;
import practice1.PacketRequest;

public class Practice2Main {
    public static void main(String[] args) throws InterruptedException {
        ServerApplication server = new ServerApplication();
        Encryptor clientEncryptor = new Encryptor();

        server.start();

        server.send(createPacket(clientEncryptor, Processor.ADD_QUANTITY, "buckwheat;10"));

        server.send(createPacket(clientEncryptor, Processor.ADD_QUANTITY, "buckwheat;20"));

        server.send(createPacket(clientEncryptor, Processor.REMOVE_QUANTITY, "buckwheat;5"));

        server.send(createPacket(clientEncryptor, Processor.GET_QUANTITY, "buckwheat"));

        server.finishSending();
        server.waitUntilFinished();

        System.out.println("Quantity of buckwheat: " + server.getProcessor().getQuantity("buckwheat"));
    }

    private static byte[] createPacket(Encryptor encryptor, int commandType, String text) {
        PacketMessage message = new PacketMessage(commandType, 1, text);

        PacketRequest request = new PacketRequest(1, message);

        return encryptor.enty_take(request);
    }
}