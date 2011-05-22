package net.sf.appia.test.ADEB;

import net.sf.appia.core.events.SendableEvent;

public class ReadyEvent extends SendableEvent {

    private String srcProcess; //this is the sender
    private String broadcastMessage;
    private String sender;
    
    
    
    public void setBroadcastMessage(String broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }
    public String getBroadcastMessage() {
        return broadcastMessage;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getSender() {
        return sender;
    }
    public void setSrcProcess(String srcProcess) {
        this.srcProcess = srcProcess;
    }
    public String getSrcProcess() {
        return srcProcess;
    }   
    
}
