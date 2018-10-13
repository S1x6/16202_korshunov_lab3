package server;

import java.io.Serializable;

public class Message implements Serializable {
    private int guid;
    private String text;
    private String senderName;
    private boolean confirmed = false;

    public Message(int guid, String text, String senderName) {
        this.guid = guid;
        this.text = text;
        this.senderName = senderName;
    }

    public int getGuid() {
        return guid;
    }

    public void setGuid(int guid) {
        this.guid = guid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
