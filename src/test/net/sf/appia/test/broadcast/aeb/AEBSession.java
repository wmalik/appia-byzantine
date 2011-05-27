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

import irdp.protocols.tutorialDA.utils.ProcessInitEvent;
import irdp.protocols.tutorialDA.utils.ProcessSet;
import irdp.protocols.tutorialDA.utils.SampleProcess;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.SSLSocket;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

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
import net.sf.appia.protocols.sslcomplete.SslRegisterSocketEvent;
import net.sf.appia.protocols.sslcomplete.SslUndeliveredEvent;
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

    private MyShell shell;

    Hashtable senderMessageMap; 

    private boolean sentecho;
    private boolean delivered;

    private int rank;
    private ArrayList<String> echos;
    private final static String BOTTOM = "BOTTOM";
    private int N;
    private final static int f = 1;
    private int sender_rank;

    /*For ProcessSet*/
    ProcessSet processSet;
    String processfile="";

    /**
     * Creates a new EccoSession.
     * @param l
     */
    public AEBSession(AEBLayer l) {
        super(l);
    }

    /**
     * Initializes the session using the parameters given in the XML configuration.
     * 
     * @param params The parameters given in the XML configuration.
     */
    public void init(SessionProperties params) {


        //Initialize the parameters for the algorithm
        sentecho = false;
        delivered = false;

        /*for sender*/
        senderMessageMap = new Hashtable();

        this.rank = Integer.parseInt(params.getProperty("rank"));
      
            /*ProcessSet Stuff*/
            this.processfile = params.getProperty("processfile");
            this.processSet = ProcessSet.buildProcessSet(processfile,rank);
            
            N = this.processSet.getSize();

            echos = new ArrayList<String>(N);
            for(int i=0;i<N;i++)
                echos.add(BOTTOM);
        
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
        if (ev instanceof ChannelInit) {
            System.out.println("handle Channel Init");
            handleChannelInit((ChannelInit) ev);
        }
        else if (ev instanceof ChannelClose){
            System.out.println("handle Channel Close");       
            handleChannelClose((ChannelClose) ev);
        }
        else if (ev instanceof BroadcastEvent) {
            System.out.println("handle Broadcast Event");
            handleBroadcastEvent((BroadcastEvent) ev);
        }
        else if (ev instanceof SslRegisterSocketEvent) {
            System.out.println("handle SSl register socket event");
            handleSslRSE((SslRegisterSocketEvent) ev);
        }
        else if (ev instanceof RegisterSocketEvent) {
            System.out.println("handle Reg Sock Ev");
            handleRSE((RegisterSocketEvent) ev);
        }
        else if (ev instanceof SendEvent) {
            System.out.println("handle Send Event");
            handleSendEvent((SendEvent) ev);
        }
        else if (ev instanceof EchoEvent) {
            System.out.println("handle Echo event");
            handleEchoEvent((EchoEvent) ev);
        }
        else if (ev instanceof SslUndeliveredEvent)
            System.out.println("SslUndeliveredEvent");
        else
            try {
                System.out.println("UNKNOWN EVENT");
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
    }

    
  
    
    private void handleEchoEvent(EchoEvent ev) {
        if (ev.getDir() == Direction.UP){
            
            String signature = ev.getMessage().popString();
            String alias = ev.getMessage().popString();

            Message message = ev.getMessage();

            sender_rank = message.popInt();
            int rank = message.popInt();
            String recvd_msg = message.popString();

            senderMessageMap.put(recvd_msg, sender_rank);
            
            if(echos.get(rank).equals(BOTTOM))
            {
                
                echos.set(rank, recvd_msg);
                System.out.println("Echo collected from process_"+rank + ": "+recvd_msg);
            }

            String msg = checkMajority(echos);

            if(msg!=null && delivered == false) {
                delivered = true;
                Deliver(msg);

            }


        }


    }


    private void Deliver(String msg) {
           
        DeliverEvent de = new DeliverEvent();
        final Message messageSend = de.getMessage();
        messageSend.pushString(msg);
        de.getMessage().pushInt(this.rank);
        de.getMessage().pushInt((Integer)senderMessageMap.get(msg));

        try {
            de.setSourceSession(this);
            de.setChannel(channel);
            de.setDir(Direction.UP);
            de.init();
            de.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
        }            
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
                      //  System.out.println("ECHOS not same - BYZANTINE DETECTED");
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
          
            String signature = ev.getMessage().popString();
            String alias = ev.getMessage().popString();
            
           // System.out.println("SENDEVENT - signature:"+ signature + " alias:"+alias);
            
            sentecho = true;
            sender_rank = ev.getMessage().popInt();

             EchoEvent ee = new EchoEvent();
                final Message messageSend = ee.getMessage();
                String myString = ev.getMessage().popString();
                ev.getMessage().pushString(myString);
                messageSend.pushString(myString);
                ee.getMessage().pushInt(this.rank);
                ee.getMessage().pushInt(sender_rank);
                ee.dest = new AppiaMulticast(null,processSet.getAllSockets());



                try {
                    System.out.println("[ECHO] Trying AppiaMulticast");
                    ee.setSourceSession(this);
                    ee.setChannel(channel);
                    ee.setDir(Direction.DOWN);
                    ee.init();
                    ee.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }            
           
            Message message = ev.getMessage();
            ev.setBroadcastMessage(message.popString());
            final long now = time.currentTimeMillis();
            System.out.print("\n process_" + this.rank+" [SEND EVENT] On ["+new Date(now)+"] : "+ev.getBroadcastMessage()+"\n> ");

        }


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
            e.printStackTrace();//
        }

        /*
         * This event is used to register a socket on the layer that is used 
         * to interface Appia with sockets.
         */
        //SslRegisterSocketEvent rse;
        RegisterSocketEvent rse;
        try {
            /*added for ProcessSet*/
            InetSocketAddress selflocal = (InetSocketAddress) processSet.getSelfProcess().getSocketAddress();

                rse = new RegisterSocketEvent(channel,Direction.DOWN,this,selflocal.getPort());   

            try {
                /*Added for ProcessSet*/
                rse.localHost = InetAddress.getByName(selflocal.getAddress().getHostAddress()); //this lets us use 127.0.0.1
            } catch (UnknownHostException e) {
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
        System.out.println("handleRSE called");
    
        shell = new MyShell(channel);
        final Thread t = event.getChannel().getThreadFactory().newThread(shell);
        t.setName("Ecco shell");
        t.start();
    }


    /*
     * SSL RegisterSocketEvent
     */
    private void handleSslRSE(SslRegisterSocketEvent event) {
        if(event.error){
            System.err.println("Error on the RegisterSocketEvent!!!");
            System.exit(-1);
        }
        System.out.println("HandleSslRSE called");
         
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
             

                SendEvent se = new SendEvent();
                final Message messageSend = se.getMessage();
                messageSend.pushString(ev.getText());
                messageSend.pushInt(rank);
                se.dest = new AppiaMulticast(null,processSet.getAllSockets());
                try {
                    System.out.println("Trying AppiaMulticast..");
                    se.setSourceSession(this);
                    se.setChannel(channel);
                    se.setDir(Direction.DOWN);
                    se.init();
                    se.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }            

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
