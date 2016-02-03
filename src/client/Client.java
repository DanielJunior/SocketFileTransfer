/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.ClientRequestProcessor;
import utils.Constants;
import utils.TypeRequest;

/**
 *
 * @author danieljr
 */
public class Client {

    private int port = 3000;
    private String ip = "localhost";
    private Socket server;

    public Client(int port, String ip) throws IOException {
        this.port = port;
        this.ip = ip;
        server = new Socket(this.ip, this.port);
    }

    public Client() throws IOException {
        server = new Socket(this.ip, this.port);
    }

    public String sendCommand(String request) throws InterruptedException {
        String resp = "";
        String args[] = request.split(" ");
        String command = args[Constants.COMMAND];
        Scanner in;
        PrintWriter out;
        String received;
        try {
            in = new Scanner(server.getInputStream());
            out = new PrintWriter(server.getOutputStream());
            switch (command) {
                case TypeRequest.LIST:
                    resp = list(out, in);
                    break;
                case TypeRequest.CREATE:
                    resp = create(out, in, args[Constants.FILENAME]);
                    break;
                case TypeRequest.GET:
                    resp = retrieve(args[Constants.FILENAME], in, out);
                    break;
                default:
                    resp = TypeRequest.NOT_FOUND;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    private String list(PrintWriter out, Scanner in) {
        String received;
        out.println(TypeRequest.LIST);
        out.flush();
        while ((received = in.nextLine()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return received;
    }

    private String create(PrintWriter out, Scanner in, String arg) {
        String resp = null;
        try {
            FileInputStream myFileStream = null;
            BufferedInputStream myFileBuffer = null;
            OutputStream byteOutput = server.getOutputStream();
            File myFile = new File(arg);

            out.println(TypeRequest.CREATE + " " + arg + " " + myFile.length());
            out.flush();

            byte[] myFileByteArray = new byte[(int) myFile.length()];
            myFileStream = new FileInputStream(myFile);
            myFileBuffer = new BufferedInputStream(myFileStream);
            myFileBuffer.read(myFileByteArray, 0, myFileByteArray.length);

            byteOutput.write(myFileByteArray, 0, myFileByteArray.length);
            byteOutput.flush();

            resp = TypeRequest.SUCCESS;
        } catch (FileNotFoundException ex) {
            resp = TypeRequest.FAIL;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            resp = TypeRequest.FAIL;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        in.nextLine();
        return resp;
    }

    public void close() {
        try {
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String retrieve(String filename, Scanner in, PrintWriter out) {
        String resp = null;
        try {
            out.println(TypeRequest.GET + " " + filename);
            out.flush();
            FileOutputStream downloaded = new FileOutputStream(new File(filename));
            String line = null;
            while ((line = in.nextLine()) == null) {
                Thread.sleep(100);
            }
            byte[] bytesReceived = new byte[Integer.parseInt(line)];
            out.println(TypeRequest.OK);
            out.flush();
            BufferedOutputStream downloadBuffer = new BufferedOutputStream(downloaded);
            while (server.getInputStream().available() <= 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            int bytesRead = server.getInputStream().read(bytesReceived, 0, bytesReceived.length);
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
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }

}
