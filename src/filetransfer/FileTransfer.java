/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransfer;

import client.Client;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;
import utils.TypeRequest;

/**
 *
 * @author danieljr
 */
public class FileTransfer {

    static Server s;
    static Client c;
    static Scanner in;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        in = new Scanner(System.in);
        System.out.println("Bem-vindo ao SocketFileTransfer!");
        System.out.println("Escolha uma das opções abaixo:");
        System.out.print("1-Servidor\n2-Cliente\n->");
        int option = in.nextInt();
        switch (option) {
            case 1:
                s = new Server();
                s.start();
                break;
            case 2:
                c = new Client();
                clientMenu();
                break;
            default:
                System.out.println("Opção inválida!");
                break;
        }
    }

    static void clientMenu() {
        int option = 0;
        do {
            System.out.println("Escolha uma opção:");
            System.out.print("1-Listar arquivos do servidor\n2-Enviar arquivo para o servidor\n3-Recuperar arquivo do servidor\n0-Sair\n->");
            option = in.nextInt();
            in.nextLine();
            switch (option) {
                case 1:
                    list();
                    break;
                case 2:
                    create();
                    break;
                case 3:
                    retrieve();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida!\n");
                    break;
            }
        } while (option != 0);
        c.close();

    }

    static void list() {
        String resp = null;
        try {
            resp = c.sendCommand(TypeRequest.LIST);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("");
        System.out.println("Arquivos Listados:");
        String[] files = resp.split(";");
        if (files[0].equalsIgnoreCase(TypeRequest.EMPTY)) {
            System.out.println("Não há arquivos disponíveis.");
        } else {
            for (String f : files) {
                System.out.println(f);
            }
        }
        System.out.println("");
    }

    private static void create() {
        System.out.println("");
        System.out.println("Digite o caminho do arquivo:");
        String filename = in.nextLine();
        try {
            String resp = c.sendCommand(TypeRequest.CREATE + " " + filename);
            System.out.println("Status: " + resp);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("");
    }

    private static void retrieve() {
        System.out.println("");
        System.out.println("Digite o nome do arquivo:");
        String filename = in.nextLine();
        try {
            String resp = c.sendCommand(TypeRequest.GET + " " + filename);
            System.out.println("Status: " + resp);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("");
    }
}
