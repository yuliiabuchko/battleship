package uj.java2019.w11.pong;

import java.io.*;
import java.net.Socket;

public class BattleshipSession implements Runnable {
    private final Socket socket;
    private final BattleShipProtocol mode;
    private final BufferedWriter out;
    private final BufferedReader in;

    private BattleshipGame gra;

    BattleshipSession(Socket socket, BattleShipProtocol mode, String fileName) throws IOException {
        gra = new BattleshipGame(fileName);
        this.socket = socket;
        this.mode = mode;
        out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void run() {
        try {
            if (mode == BattleShipProtocol.CLIENT) {
                sendStart();
            }
            do {
                String inputLine = in.readLine();
                System.out.println("[" + Thread.currentThread().getName() + "] got " + inputLine + "!");
                gra.handleEnemyResponse(inputLine);
            } while (!send());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean send() throws IOException {
        String toSend = gra.fullResponseForEnemy();
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("[" + Thread.currentThread().getName() + "] " + mode + " sending " + toSend + "...");
        out.write(toSend);
        out.newLine();
        out.flush();
        return toSend.equals(gra.gameLost + '\n');
    }

    private void sendStart() throws IOException {
        String toSend = "start;" + gra.randomWhereToShotAtEnemy();
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("[" + Thread.currentThread().getName() + "] " + mode + " sending " + toSend + "...");
        out.write(toSend);
        out.newLine();
        out.flush();
    }


}
