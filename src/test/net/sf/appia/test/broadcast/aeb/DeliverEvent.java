package net.sf.appia.test.broadcast.aeb;

import net.sf.appia.core.events.SendableEvent;

public class DeliverEvent extends SendableEvent {

   
    private String broadcastMessage;
    private String aebSender;
    
    
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
