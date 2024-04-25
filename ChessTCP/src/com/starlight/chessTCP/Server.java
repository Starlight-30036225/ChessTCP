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

public abstract class Server implements Runnable {

    protected final ArrayList<ConnectionHandler> clients;
    private ServerSocket boss;
    private boolean done;
    private final int port;

    public Server(int port) {
        System.out.println("checkpoint 1");
        this.port = port;
        clients = new ArrayList<>();
        done = false;
        System.out.println("checkpoint 1");
    }

    public void run() {
        try {
            boss = new ServerSocket(port);  //Creates a server socket at port given in constructor
            ExecutorService pool = Executors.newCachedThreadPool();
            int IDCounter = 0;
            while (!done) {
                Socket client = boss.accept();  //Accepts incoming connection

                ConnectionHandler handler = new ConnectionHandler(client, IDCounter++);  //Binds client to a connection handler, assigns an ID according to

                clients.add(handler);

                pool.execute(handler);  //Places new connection handler into thread pool to execute separately
            }
        } catch (Exception e) {     //Any errors during this loop will be caught here, shutdown the server and print the error
            System.out.println(e);
            this.shutdown();
        }
    }


    public void shutdown() {    //Something has gone wrong or game is over, shutdown Server
        try {
            done = true;
            for (ConnectionHandler ch : clients) {
                ch.localShutdown();    //shuts down all attached clients
            }
            if (!boss.isClosed()) {
                boss.close();
            }

        } catch (Exception ignored){
            System.out.println(ignored);
        }
    }

    public abstract void handlePacket(ConnectionHandler handler, PacketHeader packetHeader);

    protected abstract void sendWelcomeMessage(ConnectionHandler connectionHandler);

    class ConnectionHandler implements Runnable {

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        //private String nickname;

        public int ID;


        public ConnectionHandler(Socket client, int ID) {
            this.client = client;
            this.ID = ID;
        }

        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                sendWelcomeMessage(this);
                while (!done) {
                    if(client.isClosed()){
                        throw new RuntimeException("Client disconnected");
                    }
                    receivePacketHeader();
                }
            } catch (Exception e) {
                this.localShutdown();
            }

        }


        public String readNextString(){
            try {
                return in.readLine();

            } catch (Exception ignored) {
                return null;
            }

        }
        public void sendMessage(PacketHeader header, String message) {   //Useless rn but will hold error checking and packet selecting later
            out.println(header.toString());
            out.println(message);
        }

        void receivePacketHeader() {
            try {
                String packetString = readNextString();
                handlePacket(this, PacketHeader.valueOf(packetString));

            } catch (Exception ignored) {
                //this.localShutdown();
            }

        }

        public void localShutdown() { //Something has gone wrong or game is over, terminate Client Connection
            try {
                if (!client.isClosed()) {
                    //Tell client its being disconnected here
                    client.close();
                }
                in.close();
                out.close();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(8201);
        server.run();

    }
}

