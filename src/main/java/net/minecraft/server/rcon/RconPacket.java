package net.minecraft.server.rcon;

import java.io.*;

public class RconPacket {
    public int requestId;
    public int type;
    public String body;

    // RCON packet types
    public static final int RESPONSE_VALUE = 0;
    public static final int COMMAND = 2;
    public static final int LOGIN = 3;

    public static RconPacket read(DataInputStream in) throws IOException {
        int length = Integer.reverseBytes(in.readInt()); // Little Endian
        int requestId = Integer.reverseBytes(in.readInt());
        int type = Integer.reverseBytes(in.readInt());

        byte[] data = new byte[length - 8]; // minus requestId + type
        in.readFully(data);
        String body = new String(data, 0, data.length - 2, "UTF-8"); // strip 2 null bytes

        RconPacket packet = new RconPacket();
        packet.requestId = requestId;
        packet.type = type;
        packet.body = body;
        return packet;
    }

    public void write(DataOutputStream out) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        int length = bodyBytes.length + 10; // 4+4+len+2null

        out.writeInt(Integer.reverseBytes(length));
        out.writeInt(Integer.reverseBytes(requestId));
        out.writeInt(Integer.reverseBytes(type));
        out.write(bodyBytes);
        out.writeByte(0);
        out.writeByte(0);
        out.flush();
    }
}