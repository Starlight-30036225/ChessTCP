package com.starlight.chessTCP;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

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

            while (!done) {
                String message = in.readLine();
                if (!message.isEmpty()) {   //return received message to client verbatim
                    out.println(message);
                }
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void shutdown() {    //Something has gone wrong, kill self
        try {
            out.close();
            in.close();
            if (!Boss.isClosed()) {
                Boss.close();
            }
        } catch (Exception ignored) {}

    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 9999);
        client.run();
    }
}