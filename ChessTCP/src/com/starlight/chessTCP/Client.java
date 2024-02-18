package com.starlight.chessTCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client implements Runnable {

    private Socket Boss;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String IP;
    private int port;
    public Client(String IP, int port) {
        done = false;
        this.IP = IP;
        this.port = port;
    }

    public void run() {

        try {
            Boss = new Socket(IP, port);  //Attempts to connect to server with given IP and port from constructor
            out = new PrintWriter(Boss.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(Boss.getInputStream()));
            //System.out.println("Connected to server");
            while (!done) {
                receivePacketHeader();
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void sendMessage(PacketHeader header, String message) {   //Useless rn but will hold error checking and packet selecting later
        out.println(header.toString());
        out.println(message);
    }

    public void receivePacketHeader() {
        try {
            String packetString = readNextString();
            HandlePacket(PacketHeader.valueOf(packetString));
        } catch (Exception ignored) {}
    }

    public String readNextString(){
        try {
            return in.readLine();

        } catch (Exception ignored) {
            return null;
        }

    }


    public void shutdown() {    //Something has gone wrong, kill self
        System.out.println("Shutting down");
        try {
            out.close();
            in.close();
            if (!Boss.isClosed()) {
                Boss.close();
            }
        } catch (Exception ignored) {}

    }


    protected abstract void HandlePacket(PacketHeader packetHeader);
}