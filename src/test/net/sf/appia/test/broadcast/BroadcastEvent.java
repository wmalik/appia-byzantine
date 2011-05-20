package net.sf.appia.test.broadcast;

import net.sf.appia.core.events.SendableEvent;

public class BroadcastEvent extends SendableEvent {

    private String broadcastMessage;

    public void setBroadcastMessage(String broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }
    
    
}
