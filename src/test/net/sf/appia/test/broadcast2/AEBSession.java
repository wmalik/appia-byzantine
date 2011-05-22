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
package net.sf.appia.test.broadcast2;

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
 * This class defines a EccoSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class AEBSession extends Session implements InitializableSession {

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
    public AEBSession(AEBLayer l) {
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
        if (ev instanceof ChannelInit)
            handleChannelInit((ChannelInit) ev);
        else if (ev instanceof ChannelClose)
            handleChannelClose((ChannelClose) ev);
        else if (ev instanceof BroadcastEvent)
            handleBroadcastEvent((BroadcastEvent) ev);
        else if (ev instanceof RegisterSocketEvent)
            handleRSE((RegisterSocketEvent) ev);
        else if (ev instanceof SendEvent)
            handleSendEvent((SendEvent) ev);
        else if (ev instanceof EchoEvent)
            handleEchoEvent((EchoEvent) ev);
        else
            try {
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
    }

    private void handleEchoEvent(EchoEvent ev) {
        if (ev.getDir() == Direction.UP){

            /*        //Testing purpose, Remove later
        Message message = ev.getMessage();

        int sender_rank = message.popInt();
        String recvd_msg = message.popString();

        ev.setBroadcastMessage(recvd_msg);
        final long now = time.currentTimeMillis();
        System.out.print("\n[ECHO EVENT] On ["+new Date(now)+"] : "+ev.getBroadcastMessage()+" from Process:"+sender_rank+"\n> ");
        /////////////////////////////////////////    
             *         
             */

            /*my code starts here - REMOVE this comment after successful testing*/

            Message message = ev.getMessage();

            int rank = message.popInt();
            String recvd_msg = message.popString();


            if(echos.get(rank).equals(BOTTOM))
            {
                echos.set(rank, recvd_msg);
                System.out.println("Echo collected from process_"+rank + ": "+recvd_msg);
            }

            String msg = checkMajority(echos);

            if(msg!=null && delivered == false) {
                delivered = true;
                Deliver(msg, sender_rank);

            }


            /*my code ends here*/

        }


    }


    private void Deliver(String msg, int sender_rank) {

        System.out.println("DELIVERED: \""+ msg + "\" SENDER: "+ sender_rank);
        /*
        try {

            SendableEvent sE = new SendableEvent(channel,Direction.UP,this);
            //TODO: push sender and message to the SendableEvent
            sE.setSession(INSERTSESSIONHERE);
            sE.init();
            sE.go();
            

        } catch (AppiaEventException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

*/
    }


    /***/
    private String checkMajority(ArrayList<String> myEcho) {


        int msgCount = 0;
        int correctCount = 0;

        ArrayList<String> correctEchos = new ArrayList<String>(myEcho.size());

        for(int i=0;i<myEcho.size();i++) {
            if (!echos.get(i).equals(BOTTOM)) {
                msgCount++;
                correctEchos.add(myEcho.get(i));
            }
        }
        String msg1 = null;
        
        if(correctEchos.size() > 1) {

            for(int i=0;i<correctEchos.size();i++){
                correctCount = 0;
                msg1 = correctEchos.get(i);

                for(int j=0;j<correctEchos.size();j++) {
                    String msg2 = correctEchos.get(j);
                    if(!msg1.equals(msg2)) {
                        System.out.println("ECHOS not same - BYZANTINE DETECTED");
                    }
                    else {
                        correctCount++;
                    }
                }
                
                if(checkIfValid(correctCount))
                    break;
            }
            
            if(checkIfValid(correctCount))
                return msg1 ;
            else
                return null;
        }
        else 
            return null; //this will be either 0 or 1

    }

    private boolean checkIfValid(int correctCount) {
       if (correctCount > (N + f)/2)
           return true;
       else 
           return false;
        
    }

    private void handleSendEvent(SendEvent ev) {
        if (ev.getDir() == Direction.UP && sentecho == false){  // REmember to check if p=s(actually authentication layer should do this)
            sentecho = true;
            sender_rank = ev.getMessage().popInt();
           // ev.getMessage().pushInt(sender_rank);

            for(int i=0;i < processes.size(); i++) {

                EchoEvent ee = new EchoEvent();
                final Message messageSend = ee.getMessage();
                String myString = ev.getMessage().popString();
                ev.getMessage().pushString(myString);
                messageSend.pushString(myString);
                ee.getMessage().pushInt(this.rank);
                ee.source = local;
                ee.dest = processes.get(i);



                try {
                    System.out.println("[echo]Sending to process_"+i);
                    ee.setSourceSession(this);
                    ee.setChannel(channel);
                    ee.setDir(Direction.DOWN);
                    ee.init();
                    ee.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }            

            }     


            //Testing purpose, Remove later
            Message message = ev.getMessage();
            ev.setBroadcastMessage(message.popString());
            final long now = time.currentTimeMillis();
            System.out.print("\n[SEND EVENT] On ["+new Date(now)+"] : "+ev.getBroadcastMessage()+"\n> ");
            /////////////////////////////////////////



        }

        // Testing Purpose, Remove later
        /*  else if (ev.getDir() == Direction.UP && sentecho == true){
            System.out.print("\n[SEND EVENT]: WILL NOT DELIVER MESSAGE!! ");
        }*/
        //////////////////////////////////////////////
    }

    /*
     * ChannelInit
     */
    private void handleChannelInit(ChannelInit init) {
        System.out.println("Channel init called");
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
            RegisterSocketEvent rse = new RegisterSocketEvent(channel,Direction.DOWN,this,localPort);
            try {
                rse.localHost = InetAddress.getByName(remoteHost);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            rse.go();
        } catch (AppiaEventException e1) {
            e1.printStackTrace();
        }
    }

    /*
     * RegisterSocketEvent
     */
    private void handleRSE(RegisterSocketEvent event) {
        if(event.error){
            System.err.println("Error on the RegisterSocketEvent!!!");
            System.exit(-1);
        }

        local = new InetSocketAddress(event.localHost,event.port);
        this.processes.add(0, local); //adding ourself to the processes list.

        shell = new MyShell(channel);
        final Thread t = event.getChannel().getThreadFactory().newThread(shell);
        t.setName("Ecco shell");
        t.start();
    }

    /*
     * EchoEvent
     */
    private void handleBroadcastEvent(BroadcastEvent ev) {
        final Message message = ev.getMessage();

        if (ev.getDir() == Direction.DOWN) {
            // Event is going DOWN

            //send to ourself
            /*
            SendEvent se_self = new SendEvent();
            final Message messageSend_self = se_self.getMessage();
            messageSend_self.pushString(echo.getText());
            se_self.source = local;
            se_self.dest = local;
            try {
                System.out.println("Sending to ourself");
                se_self.setSourceSession(this);
                se_self.setChannel(channel);
                se_self.setDir(Direction.DOWN);
                se_self.init();
                se_self.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }            
             */


            for(int i=0;i < processes.size(); i++) {

                SendEvent se = new SendEvent();
                final Message messageSend = se.getMessage();
                messageSend.pushString(ev.getText());
                messageSend.pushInt(rank);
                se.source = local;
                se.dest = processes.get(i);
                try {
                    System.out.println("Sending to process_"+i);
                    se.setSourceSession(this);
                    se.setChannel(channel);
                    se.setDir(Direction.DOWN);
                    se.init();
                    se.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }            

            }		    


            /*SendEvent se = new SendEvent();
            final Message messageSend = se.getMessage();
            messageSend.pushString(echo.getText());
            se.source = local;
            se.dest = remote1;
            try {
        if (ev.getDir() == Direction.UP && sentecho == false){           System.out.println("Sending to remote1");
                se.setSourceSession(this);
                se.setChannel(channel);
                se.setDir(Direction.DOWN);
                se.init();
                se.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }



            //sending to remote2 - cloning
             SendEvent se2;
            try {
                se2 = (SendEvent)se.cloneEvent();
                se2.getMessage().pushString(echo.getText());


                se2.source = local;
                se2.dest = remote2;

                try {
                    System.out.println("Sending to remote2");
                    se2.setSourceSession(this);
                    se2.setChannel(channel);
                    se2.setDir(Direction.DOWN);
                    se2.init();
                    se2.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }
            } catch (CloneNotSupportedException e1) {
                e1.printStackTrace();
            }
             */


        }
        else {
            // Event is going UP
            ev.setText(message.popString()); //try commenting this line and see if this still works
            final long now = time.currentTimeMillis();
            System.out.print("\n[BROACAST WEIRD] On ["+new Date(now)+"] : "+ev.getText()+"\n> ");
        }
    }

    /*
     * ChannelClose
     */
    private void handleChannelClose(ChannelClose close) {
        try {
            System.out.println("Channel close called");
            close.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }
    }

}
