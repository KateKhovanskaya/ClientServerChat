package org.example.chat.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        try{
            Scanner scanner = new Scanner(System.in);
            System.out.print("введите свое имя: ");
            String name = scanner.nextLine();
            InetAddress address = InetAddress.getLocalHost();
            Socket socket = new Socket(address, 5000);
            Client client = new Client(socket, name);
            InetAddress inetAddress = socket.getInetAddress();
            System.out.println("InetAddress: " + inetAddress);
            String remoteIp = inetAddress.getHostAddress();
            System.out.println("remote IP: " + remoteIp);
            System.out.println("LocalPort: " + socket.getLocalPort());

            client.listenForMessage();// будет работать в отдельном потоке
            client.sendMessage(); // будет работать в основном потоке, поэтому запускается вторым, после предыдущего
        }catch(UnknownHostException ex){
            throw new RuntimeException(ex);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }
}
