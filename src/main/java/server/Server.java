package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable {

    private static final int confirmationPeriod = 3000;

    private String name;
    private Integer lossPercentage;
    private Node parent;

    private DatagramSocket socket;

    private List<DatagramWrapper> unconfirmedMessages;
    private List<Node> children;

    private Timer timer;

    public Server(String name, Integer selfPort, Integer lossPercentage) throws SocketException {
        this.name = name;
        this.lossPercentage = lossPercentage;

        unconfirmedMessages = new Vector<>();
        socket = new DatagramSocket(selfPort);
        timer = new Timer();
    }

    public Server(String name, Integer selfPort, Integer lossPercentage, String parentIp, Integer parentPort)
            throws SocketException, UnknownHostException {
        this(name, selfPort, lossPercentage);
        this.parent = new Node(InetAddress.getByName(parentIp), parentPort);
    }

    @Override
    public void run() {
        try {
            timer.schedule(new ResendConfirmationTimerTask(unconfirmedMessages,children, parent),1000, confirmationPeriod);
            // if parent was specified, notify it that this node is its child
            if (!isRootNode()) {
                Message helloMsg = new Message(UUID.randomUUID(), "", name, Message.MsgType.REGISTER);
                DatagramWrapper datagramWrapper = new DatagramWrapper(helloMsg, parent);
                unconfirmedMessages.add(datagramWrapper);
                socket.send(datagramWrapper.convertToDatagramPacket());
            }

            // receiving packets
            while (true) {
                DatagramPacket receivedPacket = new DatagramPacket(new byte[2048], 0, 2048);
                socket.receive(receivedPacket);
                DatagramWrapper wrapper = new DatagramWrapper(receivedPacket);
                switch (wrapper.getMessage().getType()) {
                    case REGISTER:
                        Node node = new Node(receivedPacket.getAddress(),receivedPacket.getPort());
                        if (children.indexOf(node) == -1) {
                            children.add(node);
                        }
                        sendConfirmation(wrapper);
                        break;
                    case TEXT:
                            if (hasSuchUuid(wrapper.getMessage().getUuid()) == -1) {
                                System.out.println(wrapper.getMessage().getSenderName() + ": " + wrapper.getMessage().getText());
                            }
                        for (Node child:
                            children) {
                            forwardMessage(wrapper.getMessage(),child);
                        }
                        if (!isRootNode()) {
                            forwardMessage(wrapper.getMessage(),parent);
                        }
                        
                        break;
                    case CONFIRMATION:
                        confirmMessageWithUuid(wrapper.getMessage().getUuid());
                        break;
                }
            }
        } catch (IOException |
                ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void forwardMessage(Message message, Node node) throws IOException {
        Message msg = new Message(message);
        msg.setUuid(UUID.randomUUID());
        DatagramWrapper datagramWrapper = new DatagramWrapper(msg, node);
        unconfirmedMessages.add(datagramWrapper);
        sendMessage(datagramWrapper);
    }


    private synchronized void sendMessage(DatagramWrapper datagramWrapper) throws IOException {
        socket.send(datagramWrapper.convertToDatagramPacket());
    }

    private boolean isRootNode() {
        return (parent == null);
    }

    private synchronized void confirmMessageWithUuid(UUID uuid) {
        for (int i = 0; i < unconfirmedMessages.size(); i++) {
            if (unconfirmedMessages.get(i).getMessage().getUuid().equals(uuid)) {
                unconfirmedMessages.remove(i);
                return;
            }
        }
    }

    private synchronized int hasSuchUuid(UUID uuid) {
        for (int i = 0; i < unconfirmedMessages.size(); i++) {
            if (unconfirmedMessages.get(i).getMessage().getUuid().equals(uuid)) {
               return i;
            }
        }
        return -1;
    }

    private void sendConfirmation(DatagramWrapper datagramWrapper) throws IOException {
        datagramWrapper.getMessage().setType(Message.MsgType.CONFIRMATION);
        sendMessage(datagramWrapper);
    }

    private class ResendConfirmationTimerTask extends TimerTask {

        private List<DatagramWrapper> unconfirmedMessages;
        private List<Node> children;
        private Node parent;

        ResendConfirmationTimerTask(List<DatagramWrapper> unconfirmedMessages, List<Node> children, Node parent) {
            this.unconfirmedMessages = unconfirmedMessages;
            this.children = children;
            this.parent = parent;
        }

        @Override
        public void run() {
            for (DatagramWrapper wrapper :
                    unconfirmedMessages) {
                wrapper.getMessage().setType(Message.MsgType.CONFIRMATION);
                try {
                    sendConfirmation(wrapper);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
