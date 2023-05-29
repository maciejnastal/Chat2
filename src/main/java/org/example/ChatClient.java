package org.example;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientName;

    public ChatClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to the server.");

            clientName = promptForName();
            sendMessage(clientName);

            new Thread(new IncomingMessageHandler()).start();
            new Thread(new OutgoingMessageHandler()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String promptForName() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String name = "";
        try {
            System.out.print("Enter your name: ");
            name = consoleReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    private void sendMessage(String message) {
        writer.println(message);
    }

    private void joinChatRoom(String chatRoomName) {
        String joinCommand = "/join " + chatRoomName;
        sendMessage(joinCommand);
    }

    private void leaveChatRoom(String chatRoomName) {
        String leaveCommand = "/leave " + chatRoomName;
        sendMessage(leaveCommand);
    }

    private void listChatRooms() {
        String listCommand = "/list";
        sendMessage(listCommand);
    }

    private void sendChatRoomMessage(String chatRoomName, String message) {
        String chatRoomMessage = "/room " + chatRoomName + " " + message;
        sendMessage(chatRoomMessage);
    }


    private void createChatRoom(String chatRoomName, String chatRoomDescription) {
        String createCommand = "/create " + chatRoomName + " " + chatRoomDescription;
        sendMessage(createCommand);
    }

    private class IncomingMessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class OutgoingMessageHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String message;
                while (true) {
                    message = consoleReader.readLine();

                    if (message.startsWith("/join")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 2) {
                            String chatRoomName = parts[1];
                            joinChatRoom(chatRoomName);
                        } else {
                            System.out.println("Invalid join command. Usage: /join <chat_room_name>");
                        }
                    } else if (message.startsWith("/leave")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 2) {
                            String chatRoomName = parts[1];
                            leaveChatRoom(chatRoomName);
                        } else {
                            System.out.println("Invalid leave command. Usage: /leave <chat_room_name>");
                        }
                    } else if (message.equals("/list")) {
                        listChatRooms();
                    } else if (message.startsWith("/room")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String chatRoomName = parts[1];
                            String chatRoomMessage = parts[2];
                            sendChatRoomMessage(chatRoomName, chatRoomMessage);
                        } else {
                            System.out.println("Invalid room message command. Usage: /room <chat_room_name> <message>");
                        }
                    } else if (message.startsWith("/create")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length >= 2) {
                            String chatRoomName = parts[1];
                            String chatRoomDescription = parts.length == 3 ? parts[2] : "";
                            createChatRoom(chatRoomName, chatRoomDescription);
                        } else {
                            System.out.println("Invalid create command. Usage: /create <chat_room_name>");
                        }
                    } else {
                        sendMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Replace with the server IP address or domain name
        int serverPort = 12347; // Replace with the server port
        ChatClient client = new ChatClient(serverAddress, serverPort);
    }
}