package practice1;

public class PacketMessage {

    private int commandType;
    private int userId;
    private String message;

    public PacketMessage(int commandType, int userId, String message) {
        this.commandType = commandType;
        this.userId = userId;
        this.message = message;
    }

    public int getCommandType() {
        return commandType;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "PacketMessage{" +
                "commandType=" + commandType +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                '}';
    }
}