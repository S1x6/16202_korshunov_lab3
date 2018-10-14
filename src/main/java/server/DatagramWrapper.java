package server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class DatagramWrapper {

    private Message message;
    private Node node;

    private static ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
    private static ObjectOutputStream oos;

    static {
        try {
            oos = new ObjectOutputStream(baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Message getMessage() {
        return message;
    }

    public DatagramWrapper(Message message, Node node) {
        this.message = message;
        this.node = node;
    }

    public DatagramWrapper(DatagramPacket packet) throws IOException, ClassNotFoundException {
        this.node = new Node(packet.getAddress(), packet.getPort());
        this.message = deserializeObject(packet.getData());
    }

    private static synchronized byte[] serializeObject(Object object) throws IOException {
        oos.writeObject(object);
        return baos.toByteArray();
    }

    private static synchronized <T> T deserializeObject(byte[] rawData) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new ByteArrayInputStream(rawData)).readObject();
    }

    public DatagramPacket convertToDatagramPacket() throws IOException {
        byte[] toSend = serializeObject(message);
        return new DatagramPacket(toSend, toSend.length, node.getAddress(), node.getPort());
    }
}
