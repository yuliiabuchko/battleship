package uj.java2019.w11.pong;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static uj.java2019.w11.pong.SrvUtil.findAddress;

public class APongServer {

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Map<SocketChannel, BattleshipGame> counters;
    private final Map<SocketChannel, ByteBuffer> buffers;
    private boolean killing = false;

    private APongServer(InetAddress address, int port) throws IOException {
        var serverAddress = new InetSocketAddress(address, port);
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(serverAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        counters = new HashMap<>();
        buffers = new HashMap<>();

        System.out.println("Running PongServer at address: " + address + ", port: " + port);
    }

    public static void main(String[] args) throws Exception {
        InetAddress addr = findAddress();
        APongServer pongServer = new APongServer(addr, 6666);
        new Thread(pongServer::run, "PongServer").start();
    }

    public void run() {
        try {
            runServer();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void runServer() throws IOException {
        while (true) {
            this.selector.select();
            Iterator keys = this.selector.selectedKeys().iterator();
            try {
                while (keys.hasNext()) {
                    SelectionKey key = (SelectionKey) keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) {
//                    if (!killing) {
//                        new Thread(SrvUtil::doKill, "[KILLER]").start();
//                        killing = true;
//                    }
                        this.accept(key);
                    } else if (key.isReadable()) {
                        if ( this.read(key)) break;
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        System.out.println("Got request from " + socket.getRemoteSocketAddress() + ", starting session");
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        counters.put(channel, new BattleshipGame("mapaServer.txt"));
        buffers.put(channel, ByteBuffer.allocate(160000));
        System.out.println("Connected to: " + remoteAddr);
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private boolean read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = buffers.get(channel);
        channel.read(buffer);
        buffer.flip();
        String fromClient = StandardCharsets.UTF_8.decode(
                buffer).toString();
        buffer.clear();
        System.out.println("[" + Thread.currentThread().getName() + "] got " + fromClient.trim() + "!");
        BattleshipGame gra = counters.get(channel);
        gra.handleEnemyResponse(fromClient);
        counters.put(channel, gra);
        channel.register(selector, SelectionKey.OP_WRITE);
        return fromClient.equals(gra.gameLost + "\n");
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        BattleshipGame gra = counters.get(channel);
        String s = gra.fullResponseForEnemy();
        counters.put(channel, gra);
        ByteBuffer toSend = ByteBuffer.allocate(1600);
        byte[] array = s.getBytes();
        toSend.put(array);
        toSend.flip();
        channel.write(toSend);
        System.out.println("[" + Thread.currentThread().getName() + "] " + BattleShipProtocol.SERVER + " sending " + s.trim() + "...");
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        channel.register(selector, SelectionKey.OP_READ);
    }

}
