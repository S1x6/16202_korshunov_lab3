package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server implements Runnable{

    private String name;
    private Integer selfPort;
    private Integer lossPercentage;
    private String parentIp;
    private Integer parentPort;

    private DatagramSocket socket;

    public Server(String name, Integer selfPort, Integer lossPercentage) throws SocketException {
        this.name = name;
        this.selfPort = selfPort;
        this.lossPercentage = lossPercentage;

        socket = new DatagramSocket(selfPort);
    }

    public Server(String name, Integer selfPort, Integer lossPercentage, String parentIp, Integer parentPort) throws SocketException {
        this(name, selfPort, lossPercentage);
        this.parentIp = parentIp;
        this.parentPort = parentPort;
    }

    @Override
    public void run() {

    }
}
