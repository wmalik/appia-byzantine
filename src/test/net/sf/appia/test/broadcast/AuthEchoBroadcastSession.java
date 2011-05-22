/**
 * Appia: Group communication and protocol composition framework library
 * Copyright 2006 University of Lisbon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * Initial developer(s): Alexandre Pinto and Hugo Miranda.
 * Contributor(s): See Appia web page for a list of contributors.
 */
 /*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.sf.appia.test.broadcast;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Session;
import net.sf.appia.core.TimeProvider;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;


/**
 * This class defines a EccoSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class AuthEchoBroadcastSession extends Session implements InitializableSession {

    
	private Channel channel;
    private TimeProvider time;
	private InetSocketAddress local;
	/*
	private InetSocketAddress remote;
	           private int localPort = -1;
    
     */
	
	/*Properties of AEB*/
	private boolean sentecho;
	private boolean delivered;
	private ArrayList<String> echos;
	private ArrayList<Integer> procs;
	private String ipAddress;
	private int localPort;
	private ArrayList<InetSocketAddress> remotes; 
	private AppShell shell;
	private int eventCount = 0;
	
    /**
     * Creates a new EccoSession.
     * @param l
     */
	public AuthEchoBroadcastSession(AuthEchoBroadcastLayer l) {
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
	    

	    
	    
	    ipAddress = new String(params.getProperty("ipaddr"));
	    localPort = Integer.parseInt(params.getProperty("localport"));
	    
	    procs = new ArrayList<Integer>();
	    procs.add(Integer.parseInt(params.getProperty("remoteport1")));
	    //procs.add(Integer.parseInt(params.getProperty("remoteport2")));
	    //procs.add(Integer.parseInt(params.getProperty("remoteport3")));

	    System.out.println("procs.size"+procs.size());
	    
		try {
		    this.local = new InetSocketAddress(InetAddress.getByName(ipAddress),localPort);
		    this.remotes = new ArrayList<InetSocketAddress>();
			this.remotes.add( new InetSocketAddress(InetAddress.getByName(ipAddress), procs.get(0)));
			//this.remotes.add(  new InetSocketAddress(InetAddress.getByName(ipAddress), procs.get(1)));
			//this.remotes.add(  new InetSocketAddress(InetAddress.getByName(ipAddress), procs.get(2)));
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
        //this.localPort = localPort;
		//this.remote = remote;
	}

    /**
     * Main event handler.
     * @param ev the event to handle.
     * 
     * @see net.sf.appia.core.Session#handle(net.sf.appia.core.Event)
     */
	public void handle(Event ev) {
        eventCount++;
        
	    if (ev instanceof ChannelInit)
	    {

	        show("("+eventCount+") ChannelInit");
			handleChannelInit((ChannelInit) ev);
			
	    }
        else if (ev instanceof ChannelClose)
        {
        
            show("("+eventCount+") ChannelClose");
            handleChannelClose((ChannelClose) ev);
        }
        else if (ev instanceof RegisterSocketEvent)
        {
        
            show("("+eventCount+") RegisterSocketEvent");
            handleRSE((RegisterSocketEvent) ev);
        }
        else if (ev instanceof BroadcastEvent)
        {
        
            show("("+eventCount+") BroadcastEvent");
            handleBroadcastEvent((BroadcastEvent) ev);
        }
        else if (ev instanceof EchoEvent)
        {
        
            show("("+eventCount+") EchoEvent");
            handleEchoEvent((EchoEvent) ev);
        }
        else if (ev instanceof SendEvent)
        {
            
            show("("+eventCount+") SendEvent");
            handleSendEvent((SendEvent) ev);
        }
        else
            try {
           
                show("("+eventCount+") Event");
				ev.go();
			} catch (AppiaEventException e) {
				e.printStackTrace();
			}
	}

    private void show(String string) {
        System.out.println(string);
    }

    private void handleSendEvent(SendEvent ev) {
        
        System.out.println("handleSEND: "+ev.getBroadcastMessage());
        //String senderID = String.valueOf(((InetSocketAddress)(ev.source)).getPort()).trim();

        if( sentecho == false /*&& ev.getAebSender().equals(senderID)*/ ) {
        sentecho = true;
        for (int i=0; i < procs.size(); i++) {
            
            EchoEvent ee = new EchoEvent();
            ee.setProcessID(String.valueOf(localPort));
            ee.setBroadcastMessage(ev.getBroadcastMessage());
            ee.source = local;
            ee.dest = remotes.get(i);
            
            try {
                ee.setSourceSession(this);
                ee.init();
                ee.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
            
        }
        }
       
        
    }

    private void handleEchoEvent(EchoEvent ev) {
        
        System.out.println("handleECHO: "+ev.getBroadcastMessage());
        
    }

    private void handleBroadcastEvent(BroadcastEvent ev) {
   
        System.out.println("procs.size"+procs.size());
        
      if(ev.getDir() == Direction.DOWN)
      {
          show("Broadcast event going down");
        ev.source = local;
        ev.dest = remotes.get(0);
        try {
            ev.setSourceSession(this);
            ev.init();
            ev.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }
      }
      
      else {
          show("Broadcast event going up");
      }
        
        /*
           for (int i=0; i < procs.size(); i++) {
                
                SendEvent se = new SendEvent();
                se.setProcessID(String.valueOf(localPort));
                se.setBroadcastMessage(ev.getBroadcastMessage());
                se.setAebSender(String.valueOf(localPort));
                se.source = local;
                se.dest = remotes.get(i);
                se.setDir(Direction.DOWN);
                se.getMessage().pushString("Wasif ROCKS!");
                
                
                try {
                    se.setSourceSession(this);
                    se.setChannel(channel);
                    se.init();
                    se.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }
                
            }*/
           
           
           /* Send to ourself?
           SendEvent se = new SendEvent();
           se.setProcessID(String.valueOf(localPort));
           se.setBroadcastMessage(ev.getBroadcastMessage());
           se.source = local;
           se.dest = local;*/
           
        
    }
    
    
    /*
     * handle RSE event
     * */
    private void handleRSE(RegisterSocketEvent event) {
        if(event.error){
            System.err.println("Error on the RegisterSocketEvent!!!");
            System.exit(-1);
        }

        System.out.println("Reminder: event.localHost not set by us.");
        local = new InetSocketAddress(event.localHost,event.port);
        
        shell = new AppShell(channel, this);
        final Thread t = event.getChannel().getThreadFactory().newThread(shell);
        t.setName("App shell");
        t.start();
        
        System.out.println("BLA BLA after shell");
        
    }


    /*
     * ChannelInit
     */
    private void handleChannelInit(ChannelInit init) {
        channel = init.getChannel();
        time = channel.getTimeProvider();
        try {
            
            /*Added for AEB*/ //moved to init
            sentecho = false;
            delivered = false;
            echos = new ArrayList<String>();
            
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
     * ChannelClose
     */
    private void handleChannelClose(ChannelClose close) {
        try {
            close.go();
            /*Added for AEB*/
            sentecho = false;
            delivered = false;
            echos = null;
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }
    }
    
}
