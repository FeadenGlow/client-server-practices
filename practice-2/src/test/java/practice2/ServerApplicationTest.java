package practice2;

import org.junit.jupiter.api.Test;
import practice1.Decryptor;
import practice1.Encryptor;
import practice1.PacketMessage;
import practice1.PacketRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerApplicationTest {

    @Test
    public void shouldProcessThreeCommands() throws InterruptedException {
        ServerApplication server = new ServerApplication();
        Encryptor clientEncryptor = new Encryptor();

        server.start();

        server.send(createPacket(clientEncryptor, Processor.ADD_QUANTITY, "buckwheat;10"));

        server.send(createPacket(clientEncryptor, Processor.ADD_QUANTITY, "buckwheat;20"));

        server.send(createPacket(clientEncryptor, Processor.REMOVE_QUANTITY, "buckwheat;5"));

        server.send(createPacket(clientEncryptor, Processor.GET_QUANTITY, "buckwheat"));

        server.finishSending();
        server.waitUntilFinished();

        assertThat(server.getProcessor().getQuantity("buckwheat"))
                .isEqualTo(25);

        assertThat(server.getSender().getSentMessages())
                .hasSize(4);

        byte[] lastEncryptedResponse = server.getSender().getSentMessages().get(3);

        PacketMessage lastResponse = new Decryptor().readPacket(lastEncryptedResponse).getPacketMessage();

        assertThat(lastResponse.getMessage())
                .isEqualTo("OK;quantity=25");
    }

    @Test
    public void shouldProcessMessagesSentFromManyThreads() throws InterruptedException {
        ServerApplication server = new ServerApplication();
        Encryptor clientEncryptor = new Encryptor();

        server.start();

        int threadCount = 50;
        List<byte[]> packets = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            packets.add(createPacket(clientEncryptor, Processor.ADD_QUANTITY, "buckwheat;1"));
        }

        for (byte[] packet : packets) {
            Thread thread = new Thread(() -> {
                try {
                    server.send(packet);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        server.finishSending();
        server.waitUntilFinished();

        assertThat(server.getProcessor().getQuantity("buckwheat"))
                .isEqualTo(50);

        assertThat(server.getSender().getSentMessages())
                .hasSize(50);
    }

    private byte[] createPacket(Encryptor encryptor, int commandType, String text) {
        PacketMessage message = new PacketMessage(commandType, 1, text);

        PacketRequest request = new PacketRequest(1, message);

        return encryptor.enty_take(request);
    }
}