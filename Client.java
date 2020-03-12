package uj.java2019.w11.pong;

import java.io.IOException;
import java.net.Socket;

public class PongClient {

    private static final String HOST = "10.60.254.190";
    private static final int PORT = 6666;

    public static void main(String[] args) throws IOException {
        PongClient c = new PongClient(HOST, PORT, "mapaClient.txt");
    }

    PongClient(String host, int port, String fileName) throws IOException {
        Socket s = new Socket(host, port);
        BattleshipSession session = new BattleshipSession(s, BattleShipProtocol.CLIENT, fileName);
        new Thread(session, "PongClient").start();
    }

}

