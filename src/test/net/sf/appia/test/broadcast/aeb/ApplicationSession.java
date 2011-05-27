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
package net.sf.appia.test.broadcast.aeb;

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
import net.sf.appia.core.events.AppiaMulticast;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.test.broadcast.aeb.EchoEvent;
import net.sf.appia.test.broadcast.aeb.MyShell;
import net.sf.appia.test.broadcast.aeb.SendEvent;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;


/**
 * This class defines a EccoSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class ApplicationSession extends Session implements InitializableSession {

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
    private boolean go = true;


    /**
     * Creates a new EccoSession.
     * @param l
     */
    public ApplicationSession(ApplicationLayer l) {
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
        if (ev instanceof DeliverEvent){
          //  System.out.println("[APPLICATION LAYER] DELIVER EVENT");
            handleDeliverEvent((DeliverEvent) ev);
        }
        else{
            try {
                if(ev.getDir() == Direction.DOWN)
                    System.out.println("[APPLICATION LAYER] "+ ev.getClass().getName());
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleDeliverEvent(DeliverEvent ev) {
        if (ev.getDir() == Direction.UP){
            Message message = ev.getMessage();
            int sender_rank = message.popInt();
            int rank = message.popInt();
            String recvd_msg = message.popString();
            
            System.out.println("[Application Layer] DELIVERED: \""+ recvd_msg + "\" SENDER: "+ sender_rank);
        }
        
    }
}


