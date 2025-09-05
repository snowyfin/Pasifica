package net.minecraft.server.rcon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RconServer implements Runnable {
    private final int port;
    private final String password;
    private final org.bukkit.Server server;
    private volatile boolean running = true;

    public RconServer(org.bukkit.Server server, int port, String password) {
        this.server = server;
        this.port = port;
        this.password = password;
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            while (running) {
                Socket client = socket.accept();
                new Thread(new RconClientHandler(client, server, password)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}
