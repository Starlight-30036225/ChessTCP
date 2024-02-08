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
            int IDCounter = 0;
            while (!done) {
                Socket client = boss.accept();  //Accepts incoming connection
                ConnectionHandler handler = new ConnectionHandler(client, IDCounter++);  //Binds client to a connection handler, assigns an ID according to
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
                ch.CHshutdown();
            }
        } catch (Exception ignored){}
    }

    public abstract void HandlePacket(ConnectionHandler Handler, PacketHeader packetHeader);

    protected abstract void SendWelcomeMessage(ConnectionHandler connectionHandler);

    class ConnectionHandler implements Runnable {

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        private int ID;

        private boolean active;

        public ConnectionHandler(Socket client, int ID) {
            this.client = client;
            this.ID = ID;
        }

        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                SendWelcomeMessage(this);
                while (!done) {
                    receivePacketHeader();
                }
            } catch (Exception e) {
                this.CHshutdown();
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

        public void receivePacketHeader() {
            try {
                String packetString = readNextString();
                HandlePacket(this, PacketHeader.valueOf(packetString));

            } catch (Exception ignored) {}

        }

        public void CHshutdown() { //Something has gone wrong or game is over, terminate Client Connection
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
        GameServer server = new GameServer(9999);
        server.run();

    }
}

