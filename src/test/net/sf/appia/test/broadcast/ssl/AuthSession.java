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
package net.sf.appia.test.broadcast.ssl;

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
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;


/**
 * This class defines a AuthSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class AuthSession extends Session implements InitializableSession {

    private Channel channel;
    private TimeProvider time;

    private InetSocketAddress local;
    private int localPort = -1;
    private ArrayList<InetSocketAddress> processes; 

    private MyShell shell;


    private String remoteHost;

    private boolean sentecho;
    private boolean delivered;

    private int rank;
    private ArrayList<String> echos;
    private final static String BOTTOM = "BOTTOM";
    private int N;
    private final static int f = 1;
    private int sender_rank;


    /**
     * Creates a new EccoSession.
     * @param l
     */
    public AuthSession(AuthLayer l) {
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

        //Initialise the parameters for the algorithm

        sentecho = false;
        delivered = false;
        processes = new ArrayList<InetSocketAddress>();

        ///////////////////////////////////////


        this.rank = Integer.parseInt(params.getProperty("rank"));
        this.localPort = Integer.parseInt(params.getProperty("localport"));
        remoteHost = params.getProperty("remotehost");
        final int remotePort1 = Integer.parseInt(params.getProperty("remoteport1"));
        final int remotePort2 = Integer.parseInt(params.getProperty("remoteport2"));
        final int remotePort3 = Integer.parseInt(params.getProperty("remoteport3"));


        try {
            InetSocketAddress remote1 = 	new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort1);
            InetSocketAddress remote2 =   new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort2);
            InetSocketAddress remote3 =   new InetSocketAddress(InetAddress.getByName(remoteHost),remotePort3);


            /*added for the for loop*/
            this.processes.add(remote1);
            this.processes.add(remote2);
            this.processes.add(remote3);

            N = this.processes.size() + 1;

            echos = new ArrayList<String>(N);
            for(int i=0;i<this.processes.size()+1;i++)
                echos.add(BOTTOM);

        } catch (UnknownHostException e) {
            e.printStackTrace(); //
        }
    }


    /**
     * Main event handler.
     * @param ev the event to handle.
     * 
     * @see net.sf.appia.core.Session#handle(net.sf.appia.core.Event)
     */
    public void handle(Event ev) {

        if (ev instanceof SendableEvent)
            handleSendableEvent((SendableEvent) ev);
        else
            try {
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
    }


    private void handleSendableEvent(SendableEvent ev) {
        if (ev.getDir() == Direction.UP){  // REmember to check if p=s(actually authentication layer should do this)

        }
        else {// Direction.DOWN
            Message msg = ev.getMessage();
            //TODO: Encrypt msg or what?
            
        }

    }




}
