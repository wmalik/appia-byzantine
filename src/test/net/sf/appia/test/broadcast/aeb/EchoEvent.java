package net.sf.appia.test.broadcast.aeb;

import net.sf.appia.core.events.SendableEvent;

public class EchoEvent extends SendableEvent {

    private String processID; //this is the sender
    private String broadcastMessage;
    private String aebSender;
    
    public void setProcessID(String processID) {
        this.processID = processID;
    }
    public String getProcessID() {
        return processID;
    }
    
    public void setBroadcastMessage(String broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }
    public String getBroadcastMessage() {
        return broadcastMessage;
    }
    public void setAebSender(String aebSender) {
        this.aebSender = aebSender;
    }
    public String getAebSender() {
        return aebSender;
    }   
    
}
