package net.minecraft.server.rcon;

import java.io.*;
import java.net.Socket;
import org.bukkit.command.ConsoleCommandSender;

public class RconClientHandler implements Runnable {
    private final Socket client;
    private final String password;
    private final org.bukkit.Server server;
    private boolean authed = false;

    public RconClientHandler(Socket client, org.bukkit.Server server, String password) {
        this.client = client;
        this.server = server;
        this.password = password;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(client.getInputStream());
             DataOutputStream out = new DataOutputStream(client.getOutputStream())) {

            while (!client.isClosed()) {
                RconPacket packet = RconPacket.read(in);

                if (packet.type == RconPacket.LOGIN) {
                    if (packet.body.equals(password)) {
                        authed = true;
                        packet.write(out); // return same requestId
                    } else {
                        RconPacket fail = new RconPacket();
                        fail.requestId = -1;
                        fail.type = RconPacket.RESPONSE_VALUE;
                        fail.body = "";
                        fail.write(out);
                        client.close();
                    }
                } else if (packet.type == RconPacket.COMMAND && authed) {
                    String command = packet.body;

                    // Dispatch as console
                    ConsoleCommandSender console = server.co();
                    boolean success = server.dispatchCommand(console, command);

                    RconPacket response = new RconPacket();
                    response.requestId = packet.requestId;
                    response.type = RconPacket.RESPONSE_VALUE;
                    response.body = success ? "OK" : "Unknown command";
                    response.write(out);
                }
            }
        } catch (IOException e) {
            // client disconnect
        }
    }
}
