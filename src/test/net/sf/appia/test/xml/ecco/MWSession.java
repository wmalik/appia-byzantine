
package net.sf.appia.test.xml.ecco;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Session;
import net.sf.appia.core.TimeProvider;
import net.sf.appia.core.TimerManager;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;


/**
 * This class defines a MWSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class MWSession extends Session implements InitializableSession {

    private Channel channel;
    private TimeProvider time;

    private InetSocketAddress local;
    private InetSocketAddress remote;
    private int localPort = -1;
    private int mwseq_num=0;

    private MyShell shell;
    private Hashtable<Integer, String> messageList = new Hashtable<Integer, String>(); 

    /**
     * Creates a new EccoSession.
     * @param l
     */
    public MWSession(MWLayer l) {
        super(l);
    }

    /**
     * Initializes the session using the parameters given in the XML configuration.
     * Possible parameters:
     * <ul>
     * <li><b>localport</b> the local port to bind.
     * <li><b>remotehost</b> the remote host (IP address).
     * <li><b>remoteport</b> the remote port.
     * </ul>
     * 
     * @param params The parameters given in the XML configuration.
     */
    public void init(SessionProperties params) {
        this.localPort = Integer.parseInt(params.getProperty("localport"));
        final String remoteHost = params.getProperty("remotehost");
        final int remotePort = Integer.parseInt(params.getProperty("remoteport"));
        try {
            this.remote = 
                new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialization method to be used directly by a static 
     * initialization process
     * @param localPort the local por
     * @param remote the remote address
     */
    public void init(int localPort, InetSocketAddress remote){
        this.localPort = localPort;
        this.remote = remote;
    }

    /**
     * Main event handler.
     * @param ev the event to handle.
     * 
     * @see net.sf.appia.core.Session#handle(net.sf.appia.core.Event)
     */
    public void handle(Event ev) {
        /*if (ev instanceof ChannelInit)
            handleChannelInit((ChannelInit) ev);
        else if (ev instanceof ChannelClose)
            handleChannelClose((ChannelClose) ev);
        else if (ev instanceof MyEccoEvent)
            handleMyEchoEvent((MyEccoEvent) ev);
        else if (ev instanceof RegisterSocketEvent) // EDIT THIS TO MAKE IT WORK!
            handleRSE((RegisterSocketEvent) ev);
        else*/

        try{
            if (ev instanceof MyEccoEvent)
                handleMongoWongo((MyEccoEvent) ev);
            else if (ev instanceof AckEvent)
                handleAckEvent((AckEvent)ev);
            else
                ev.go();
        }
        catch (AppiaEventException e) {
            e.printStackTrace();
        }

    }

    private void handleAckEvent(AckEvent ev) throws AppiaEventException {
        
        if(ev.getDir() == Direction.UP) {
            messageList.remove(ev.getSeqnum());
            System.out.println("Message "+ev.getSeqnum()+" delivered successfully");
            
        }
        
        else if(ev.getDir() == Direction.DOWN) {
            //would never happen
        }
        
    }
    
    private void handleMongoWongo(MyEccoEvent ev) throws AppiaEventException {


            //    ev.go();
            if(ev.getDir() == Direction.UP) {
                ev.go();

                
                
                //sending the ack
                AckEvent ack = new AckEvent(channel, Direction.DOWN, this);
                ack.setSeqnum(ev.getSeqnum());
                ack.go();
                
                
            }

            else if(ev.getDir() == Direction.DOWN) {
              
                
                MyEccoEvent me = (MyEccoEvent)ev;
                String txt = "("+(++mwseq_num)+") "+me.getMessage().popString();
                me.getMessage().pushString(txt);
                me.setSeqnum(mwseq_num);
                messageList.put(mwseq_num, txt);
                ev.go();
                //remove this entry when ack is received
                //start a timer of 2 seconds and if no ack is received, send the message again



            }

    }

    /*
     * ChannelInit
     */
    private void handleChannelInit(ChannelInit init) {
        channel = init.getChannel();
        time = channel.getTimeProvider();
        try {
            init.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }

        /*
         * This event is used to register a socket on the layer that is used 
         * to interface Appia with sockets.
         */
        try {
            new RegisterSocketEvent(channel,Direction.DOWN,this,localPort).go();
        } catch (AppiaEventException e1) {
            e1.printStackTrace();
        }
    }

    /*
     * RegisterSocketEvent
     */
    private void handleRSE(RegisterSocketEvent event) {
        //System.out.println("Event Dir: "+event.getDir());
        if(event.error){
            System.err.println("Error on the RegisterSocketEvent!!! "+ event.getDir() + event.getErrorDescription());
            System.exit(-1);
        }
        //        if(event.getDir() == Direction.DOWN)
        //        System.out.println("handleRSE called but not forwarded - Direction: Down");
        //        else {
        //                System.out.println("handleRSE called but not forwarded - Direction: UP");
        //        }

        try {
            event.go();
            System.out.println();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }

        local = new InetSocketAddress(event.localHost,event.port);

        //        shell = new MyShell(channel);
        //        final Thread t = event.getChannel().getThreadFactory().newThread(shell);
        //        t.setName("Ecco shell");
        //        t.start();
    }

    /*
     * EchoEvent
     */
    private void handleMyEchoEvent(MyEccoEvent echo) {
        final Message message = echo.getMessage();

        if (echo.getDir() == Direction.DOWN) { //PASSING EVERYTHING DOWN!!!!!!
            // Event is going DOWN
            //message.pushString(echo.getText());


            //echo.source = local;
            //echo.dest = remote;
            try {
                //  echo.setSourceSession(this);
                //  echo.init();
                echo.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
        }
        else {
            // Event is going UP
            /* commented out by MW
            echo.go(); 
            echo.setText(message.popString()); //try commenting this line and see if this still works
            final long now = time.currentTimeMillis();
            System.out.print("\nOn ["+new Date(now)+"] : "+echo.getText()+"\n> ");
             */

            if(echo.getDir() == Direction.UP){
                if (echo.getChannel().isStarted()) {
                    try {
                        echo.go();
                        System.out.println("[mwseq_num="+(++mwseq_num)+"]");


                    } catch (AppiaEventException e1) {
                        e1.printStackTrace();
                    }
                }
                return;
            }

        }
    }

    /*
     * ChannelClose
     */
    private void handleChannelClose(ChannelClose close) {
        try {
            close.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }
    }

}
