package com.starlight.chessTCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final ArrayList<ConnectionHandler> clients;
    private ServerSocket boss;
    private ExecutorService pool;
    private boolean done;
    private int port;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        done = false;
    }

    public void run() {
        try {
            boss = new ServerSocket(port);  //Creates a server socket at port given in constructor
            pool = Executors.newCachedThreadPool();

            while (!done) {
                Socket client = boss.accept();  //Accepts incoming connection
                ConnectionHandler handler = new ConnectionHandler(client);  //Binds client to a connection handler
                clients.add(handler);

                pool.execute(handler);  //Places new connection handler into thread pool to execute seperately
            }
        } catch (Exception e) {
            this.shutdown();
        }
    }


    public void shutdown() {    //Something has gone wrong or game is over, shutdown Server
        try {
            done = true;
            if (!boss.isClosed()) {
                boss.close();
            }
            for (ConnectionHandler ch : clients) {
                ch.shutdown();
            }
        } catch (Exception ignored){}
    }


    class ConnectionHandler implements Runnable {

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                sendMessage("Enter a nickname");
                nickname = in.readLine();
                System.out.println("nickname: " + nickname);
                sendMessage("Welcome " + nickname);
                System.out.println(in.readLine());
            } catch (Exception e) {
                this.shutdown();
            }

        }

        public void sendMessage(String message) {   //Useless rn but will hold error checking and packet selecting later
            out.println(message);
        }

        public void shutdown() { //Something has gone wrong or game is over, terminate Client Connection
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    //Tell client its being disconnected here
                    client.close();
                }
            } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) {
        Server server = new Server(9999);
        server.run();

    }
}

