/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author danieljr
 */
public class Server {

    private ServerSocket server;
    private int port = 3000;

    public Server(int port) throws IOException {
        this.port = port;
        server = new ServerSocket(port);
    }

    public Server() throws IOException {
        server = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            Socket client = server.accept();
            System.out.println("New client connected " + client.getInetAddress());
            ClientRequestProcessor requestProcessor = new ClientRequestProcessor(client);
            requestProcessor.run();
        }
    }
}
