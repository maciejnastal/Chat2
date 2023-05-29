package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private static final int serverPort = 12347;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Map<String, ChatRoom> chatRooms = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started. Listening on port " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new ClientHandler for the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static List<ChatRoom> getChatRooms() {
        return new ArrayList<>(chatRooms.values());
    }

    public static ChatRoom createChatRoom(String name, String description) {
        ChatRoom chatRoom = new ChatRoom(name, description);
        chatRooms.put(chatRoom.getName(), chatRoom);
        return chatRoom;
    }

    public static ChatRoom getChatRoomByName(String name) {
        return chatRooms.get(name);
    }

    public static void removeChatRoom(ChatRoom chatRoom) {
        chatRooms.remove(chatRoom.getName());
    }

    public static List<ChatRoom> getAllChatRooms() {
        return new ArrayList<>(chatRooms.values());
    }

    public static synchronized void createChatRoomInternal(String roomName, String roomDescription) {
        if (chatRooms.containsKey(roomName)) {
            System.out.println("The chat room '" + roomName + "' already exists.");
        } else {
            ChatRoom chatRoom = new ChatRoom(roomName, roomDescription);
            chatRooms.put(roomName, chatRoom);
            System.out.println("Chat room '" + roomName + "' created successfully.");
        }
    }

    public static synchronized void createChatRoom(String roomName, String roomDescription, ClientHandler clientHandler) {
        if (clientHandler.getCurrentRoom() != null) {
            System.out.println("You are already in a chat room. Please leave the current room before creating a new one.");
        } else {
            createChatRoomInternal(roomName, roomDescription);
            clientHandler.switchChatRoom(roomName);
        }
    }
}
