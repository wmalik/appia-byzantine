
package net.sf.appia.test.xml.ecco;

import net.sf.appia.core.Layer;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.protocols.tcpcomplete.CloseTcpSocket;
import net.sf.appia.protocols.tcpcomplete.TcpTimer;


/**
 * This class defines a MWLayer - the Mongo Wongo Layer
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class MWLayer extends Layer {

    /**
     * Creates a new EccoLayer.
     */
    public MWLayer() {
        
        /* Comments by Wasif
         * specifies which guarantees/messages are required from the lower layers
         * 
         */
        evRequire = new Class[]{
                RegisterSocketEvent.class,
                SendableEvent.class,
                ChannelInit.class,
        };
        
        /* Comments by Wasif
         * specifies which guarantees/messages are sent from this layer
         * 
         */
        
        evProvide = new Class[] {
    //      RegisterSocketEvent.class,      //so that application is bind to a socket
          //usually this class is required for only the top most layer
                RegisterSocketEvent.class,
                SendableEvent.class,
                ChannelInit.class,
                ChannelClose.class,
                TcpTimer.class,
                CloseTcpSocket.class,
        };
        
        evAccept = new Class[]{
                ChannelInit.class,
                ChannelClose.class,
                RegisterSocketEvent.class,
                MyEccoEvent.class,
        };
    }
    
    /**
     * Creates the session for this protocol.
     * @see Layer#createSession()
     */
    public Session createSession() {
        return new MWSession(this);
    }
}
