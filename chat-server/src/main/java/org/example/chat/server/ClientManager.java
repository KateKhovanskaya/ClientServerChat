package org.example.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements  Runnable{
    private Socket socket;
    private String name;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try{
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("all", "Server: " + name + " подключился к чату.");
        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClient();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("all", "Server: " + name + " покинул чат.");
    }

    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
//                Следующий if для mackOS, так с него при отключении клиента приходят сообщения null
                if(messageFromClient == null){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                String[][] messageData = getMessageData(messageFromClient);
                String messageToSend = messageData[0][1] + ": " + messageData[2][1];
                broadcastMessage(messageData[1][1], messageToSend);
            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void broadcastMessage(String addressName, String message){
        if(addressName.equals("all")){
            for(ClientManager client:clients){
                try{
                    if(!client.name.equals(name) && message != null){
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                }catch(IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }else{
            boolean addressWasFind = false;
            for(ClientManager client:clients){
                try{
                    if(client.name.equals(addressName) && message != null){
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                        addressWasFind = true;
                    }
                }catch(IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
            if(!addressWasFind){
                broadcastMessage(name, "Server: указанный пользователь не найден!");
            }
        }

    }
    private String[][] getMessageData(String messageFromClient){
        String[] messageData = messageFromClient.split(";");
        String[][] finalData = new String[messageData.length][2];
        for (int i = 0; i <= 2; i++) {
            finalData[i]= messageData[i].split(":");
        }
        return finalData;
    }
}
