package com.starlight.chessTCP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client implements Runnable {

    private Socket boss;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private final String ip;
    private final int port;
    public Client(String ip, int port) {
        done = false;
        this.ip = ip;
        this.port = port;
    }


    public void run() {

        try {
            boss = new Socket(ip, port);  //Attempts to connect to server with given IP and port from constructor
            out = new PrintWriter(boss.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(boss.getInputStream()));

            while (!done) {
                receivePacketHeader();
                if (boss.isClosed()){
                    done = true;
                }
            }
            shutdown();
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
            handlePacket(PacketHeader.valueOf(packetString));
        } catch (Exception e) {
            System.out.println(e);
        }
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
        done = true;
        try {
            out.close();
            in.close();
            if (!boss.isClosed()) {
                boss.close();
            }
        } catch (Exception ignored) {}

    }


    protected abstract void handlePacket(PacketHeader header);
}