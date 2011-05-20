package net.sf.appia.test.broadcast;

import net.sf.appia.core.Event;



public class DeliverEvent extends Event {

   
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    
  
    
}
