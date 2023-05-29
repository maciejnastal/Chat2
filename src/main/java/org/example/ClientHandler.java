package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final String clientName;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatRoom currentRoom;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientName = getClientName();
    }

    private String getClientName() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            ChatServer.broadcast(clientName + " has joined the chat.", this);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Message received from " + clientName + ": " + clientMessage);

                // Check if the message is a command to switch chat rooms
                if (clientMessage.startsWith("/join ")) {
                    String roomName = clientMessage.substring(6);
                    switchChatRoom(roomName);
                } else if (clientMessage.equals("/list")) {
                    listChatRooms();
                }
                else if (clientMessage.startsWith("/room ")) {
                    String[] parts = clientMessage.split(" ", 3);
                    if (parts.length >= 3) {
                        String chatRoomName = parts[1];
                        String chatRoomMessage = parts[2];
                        sendChatRoomMessage(chatRoomName, chatRoomMessage);
                    } else {
                        writer.println("Invalid room message command. Usage: /room <chat_room_name> <message>");
                    }
                } else if (clientMessage.startsWith("/leave ")) {
                    String roomName = clientMessage.substring(7);
                    leaveChatRoom(roomName);
                } else if (clientMessage.startsWith("/create ")) {
                        String[] parts = clientMessage.split(" ", 2);
                        if (parts.length >= 2) {
                            String[] roomParams = parts[1].split(" ", 2);
                            if (roomParams.length >= 2) {
                                String chatRoomName = roomParams[0];
                                String chatRoomDescription = roomParams[1];
                                ChatRoom chatRoom = ChatServer.createChatRoom(chatRoomName, chatRoomDescription);
                                ChatServer.broadcast("New chat room created: " + chatRoom.getName(), null);
                            } else {
                                writer.println("Invalid create command. Usage: /create <chat_room_name> <chat_room_description>");
                            }
                        } else {
                            writer.println("Invalid create command. Usage: /create <chat_room_name> <chat_room_description>");
                        }
                    } else {
                    // Broadcast the message to the current chat room
                    if (currentRoom != null) {
                        ChatServer.broadcast(currentRoom.getName() + " - " + clientName + ": " + clientMessage, this);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                writer.close();
                clientSocket.close();
                ChatServer.removeClient(this);
                System.out.println("Client disconnected: " + clientSocket);
                ChatServer.broadcast(clientName + " has left the chat.", this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void switchChatRoom(String roomName) {
        ChatRoom newRoom = ChatServer.getChatRoomByName(roomName);

        if (newRoom == null) {
            sendMessage("The chat room '" + roomName + "' does not exist.");
        } else {
            if (currentRoom != null) {
                ChatServer.broadcast(currentRoom.getName() + " - " + clientName + " has left the chat room.", this);
            }

            currentRoom = newRoom;
            sendMessage("You have joined the chat room '" + currentRoom.getName() + "'.");
            ChatServer.broadcast(currentRoom.getName() + " - " + clientName + " has joined the chat room.", this);
        }
    }
    public ChatRoom getChatRoomByName(String name) {
        return ChatServer.getChatRoomByName(name);
    }
    public void listChatRooms() {
        String chatRoomsList = "Available chat rooms:";
        for (ChatRoom chatRoom : ChatServer.getChatRooms()) {
            chatRoomsList += "\n- " + chatRoom.getName() + ": " + chatRoom.getDescription();
        }
        sendMessage(chatRoomsList);
    }

    public void sendChatRoomMessage(String chatRoomName, String message) {
        ChatRoom chatRoom = ChatServer.getChatRoomByName(chatRoomName);

        if (chatRoom == null) {
            sendMessage("The chat room '" + chatRoomName + "' does not exist.");
        } else {
            ChatServer.broadcast(chatRoom.getName() + " - " + clientName + ": " + message, this);
        }
    }

    public void leaveChatRoom(String roomName) {
        if (currentRoom == null) {
            sendMessage("You are not currently in a chat room.");
        } else if (!currentRoom.getName().equalsIgnoreCase(roomName)) {
            sendMessage("You are not in the chat room '" + roomName + "'.");
        } else {
            ChatServer.broadcast(currentRoom.getName() + " - " + clientName + " has left the chat room.", this);
            currentRoom = null;
            sendMessage("You have left the chat room '" + roomName + "'.");
        }
    }
}