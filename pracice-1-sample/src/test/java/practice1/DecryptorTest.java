package practice1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DecryptorTest {

    @Test
    public void shouldReadCreatedPacket() {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        PacketMessage message = new PacketMessage(1, 1, "{\"text\":\"Hello\"}");

        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        PacketData data = decryptor.readPacket(packet);

        assertThat(data.getSource()).isEqualTo(1);
        assertThat(data.getPacketId()).isEqualTo(1L);
        assertThat(data.getPacketMessage().getCommandType()).isEqualTo(1);
        assertThat(data.getPacketMessage().getUserId()).isEqualTo(1);
        assertThat(data.getPacketMessage().getMessage()).isEqualTo("{\"text\":\"Hello\"}");
    }

    @Test
    public void shouldFailWhenMagicByteIsWrong() {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        PacketMessage message = new PacketMessage(1, 1, "hello");
        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        packet[0] = 0x55;

        assertThrows(IllegalArgumentException.class, () -> decryptor.readPacket(packet));
    }

    @Test
    public void shouldFailWhenHeaderCrcIsWrong() {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        PacketMessage message = new PacketMessage(1, 1, "hello");
        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        packet[1] = 0x33;

        assertThrows(IllegalArgumentException.class, () -> decryptor.readPacket(packet));
    }

    @Test
    public void shouldFailWhenMessageCrcIsWrong() {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        PacketMessage message = new PacketMessage(1, 1, "hello");
        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        packet[20] = (byte) (packet[20] + 1);

        assertThrows(IllegalArgumentException.class, () -> decryptor.readPacket(packet));
    }

    @Test
    public void shouldIncreasePacketId() {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        PacketMessage firstMessage = new PacketMessage(1, 1, "first");
        PacketMessage secondMessage = new PacketMessage(1, 1, "second");

        PacketRequest firstRequest = new PacketRequest(1, firstMessage);
        PacketRequest secondRequest = new PacketRequest(1, secondMessage);

        byte[] firstPacket = encryptor.enty_take(firstRequest);
        byte[] secondPacket = encryptor.enty_take(secondRequest);

        PacketData firstData = decryptor.readPacket(firstPacket);
        PacketData secondData = decryptor.readPacket(secondPacket);

        assertThat(firstData.getPacketId()).isEqualTo(1L);
        assertThat(secondData.getPacketId()).isEqualTo(2L);
    }
}