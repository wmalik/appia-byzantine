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
package net.sf.appia.test.broadcast2Byzantine;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import net.sf.appia.core.AppiaEventException;
import net.sf.appia.core.Channel;
import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Session;
import net.sf.appia.core.TimeProvider;
import net.sf.appia.core.events.SendableEvent;
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
public class ByzantineSession extends Session implements InitializableSession {

    private Channel channel;
    private TimeProvider time;

    private InetSocketAddress local;
    private int localPort = -1;
    private ArrayList<InetSocketAddress> processes; 

    private MyShell shell;


    private boolean drop_send = false;
    private boolean drop_echo = false;
    private Integer modify_messageAtSource;
    private Integer modify_messageInBetween;


    /**
     * Creates a new EccoSession.
     * @param l
     */
    public ByzantineSession(ByzantineLayer l) {
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

        this.drop_send = Boolean.parseBoolean(params.getProperty("drop_send"));
        this.drop_echo = Boolean.parseBoolean(params.getProperty("drop_echo"));
        this.modify_messageAtSource = Integer.parseInt(params.getProperty("modify_messageAtSource"));
        this.modify_messageInBetween = Integer.parseInt(params.getProperty("modify_messageInBetween"));
        System.out.println("[INIT BYZANTINE], DROP_SEND: "+ this.drop_send);
        System.out.println("[INIT BYZANTINE], DROP_ECHO: "+ this.drop_echo);
        System.out.println("[INIT BYZANTINE], MODIFY_MESSAGE SOURCE: "+ this.modify_messageAtSource);
        System.out.println("[INIT BYZANTINE], MODIFY_MESSAGE IN BETWEEN: "+ this.modify_messageInBetween);
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
        if (ev instanceof SendEvent){
            handleSendEvent((SendEvent) ev);
        }
        else if (ev instanceof EchoEvent){
            handleEchoEvent((EchoEvent) ev);
        }
        else{
            try {
                if(ev.getDir() == Direction.DOWN)
                    System.out.println("BYZANTINE LAYER");
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleEchoEvent(EchoEvent ev) {
        
        if(modify_messageInBetween >0){
            String msg = ev.getMessage().popString();
            Random r = new Random();
            msg = Long.toString(Math.abs(r.nextLong()), 36);
            ev.setBroadcastMessage(msg);
            ev.getMessage().pushString(msg);
            modify_messageInBetween --;
        }
        
        if(!this.drop_echo)
        {
            try {
                ev.go();
            } catch (AppiaEventException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        else {
            System.out.println("[BYZANTINE] Dropping message of type Echoevent");
        }
    }

    private void handleSendEvent(SendEvent ev) {
        
        if(modify_messageAtSource>0){
            Integer rank = ev.getMessage().popInt();
            String msg = ev.getMessage().popString();
            Random r = new Random();
            msg = Long.toString(Math.abs(r.nextLong()), 36);
            System.out.println("Modified String is: " +msg);
            ev.setBroadcastMessage(msg);
            ev.getMessage().pushString(msg);
            ev.getMessage().pushInt(rank);
            modify_messageAtSource --;
        }
        
        if(!this.drop_send){
            try {
                ev.go();
            } catch (AppiaEventException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            System.out.println("[BYZANTINE] Dropping message of type sendevent");   
        }
    }

}


