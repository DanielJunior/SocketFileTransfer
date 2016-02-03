/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Constants;
import utils.TypeRequest;

/**
 *
 * @author danieljr
 */
public class ClientRequestProcessor implements Runnable {

    private Socket client;

    public ClientRequestProcessor(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        boolean running = true;
        PrintWriter out = null;
        BufferedReader in = null;
        String request, resp;
        try {
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            System.err.println("Impossível abrir fluxos de comunicação!");
        }
        while (running && client.isConnected()) {
            try {
                request = in.readLine();
                if (request != null) {
                    System.out.println(request + " from " + client.getInetAddress());
                    resp = processRequest(request);
                    out.println(resp);
                    out.flush();
                }
            } catch (IOException ex) {
                System.out.println("Client " + client.getInetAddress() + " is disconnected");
                running = false;
            }

        }
    }

    private String processRequest(String request) {
        String resp = "";
        String args[];
        String command = request.split(" ")[Constants.COMMAND];
        switch (command.trim()) {
            case TypeRequest.LIST:
                resp = list();
                break;
            case TypeRequest.CREATE:
                args = request.split(" ");
                resp = create(args[Constants.FILENAME], Integer.parseInt(args[Constants.LENGHT]));
                break;
            case TypeRequest.GET:
                args = request.split(" ");
                resp = retrieve(args[Constants.FILENAME]);
                break;
            default:
                resp = TypeRequest.NOT_FOUND;
                break;
        }
        return resp;
    }

    private String list() {
        String resp = "";
        File curDir = new File("./uploads");
        File[] filesList = curDir.listFiles();
        try {
            for (File f : filesList) {
                if (f.isFile()) {
                    resp += f.getName() + ";";
                }
            }
        } catch (NullPointerException e) {
            resp = TypeRequest.EMPTY;
        }
        return resp;
    }

    private String create(String filename, int lenght) {
        String resp = null;
        try {
            String path[] = filename.split("/");
            File file = new File("uploads");
            if (!file.exists() || !file.isDirectory()) {
                file.mkdirs();
            }
            FileOutputStream downloaded = new FileOutputStream(file + "/" + path[path.length - 1]);
            byte[] bytesReceived = new byte[lenght];
            BufferedOutputStream downloadBuffer = new BufferedOutputStream(downloaded);
            while (client.getInputStream().available() <= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            int bytesRead = client.getInputStream().read(bytesReceived, 0, bytesReceived.length);

            downloadBuffer.write(bytesReceived, 0, bytesRead);
            downloadBuffer.flush();

            downloadBuffer.close();
            resp = TypeRequest.SUCCESS;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            resp = TypeRequest.FAIL;
        } catch (IOException ex) {
            Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            resp = TypeRequest.FAIL;
        }
        return resp;
    }

    private String retrieve(String filename) {
        String resp;
        try {
            FileInputStream myFileStream = null;
            BufferedInputStream myFileBuffer = null;
            OutputStream byteOutput;

            byteOutput = client.getOutputStream();

            File myFile = new File("./uploads/" + filename);

            byte[] myFileByteArray = new byte[(int) myFile.length()];
            myFileStream = new FileInputStream(myFile);
            myFileBuffer = new BufferedInputStream(myFileStream);
            myFileBuffer.read(myFileByteArray, 0, myFileByteArray.length);

            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(myFile.length());
            out.flush();

            while (client.getInputStream().available() <= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String line = new Scanner(client.getInputStream()).nextLine();
            if (line.equals(TypeRequest.OK)) {
                byteOutput.write(myFileByteArray, 0, myFileByteArray.length);
                byteOutput.flush();

                resp = TypeRequest.SUCCESS;
            } else {
                resp = TypeRequest.FAIL;
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            resp = TypeRequest.FAIL;
        }
        return resp;
    }
}
