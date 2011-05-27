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
package net.sf.appia.test.ADEB;

import irdp.protocols.tutorialDA.utils.ProcessSet;

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
import net.sf.appia.core.events.AppiaMulticast;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelClose;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.protocols.sslcomplete.SslRegisterSocketEvent;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;


/**
 * This class defines a EccoSession.
 * 
 * @author Jose Mocito
 * @version 1.0
 */
public class ADEBSession extends Session implements InitializableSession {

    private Channel channel;
    private TimeProvider time;

    private MyShell shell;


    private boolean sentecho;
    private boolean sentready;
    private boolean delivered;
    private ArrayList<String> echos;
    private ArrayList<String> readys;
    
    private int rank;
    
    private final static String BOTTOM = "BOTTOM";
    private int N;
    private final static int f = 1;
    private int sender_rank;
    
    /* SSL */
    private boolean ssl=false;
    private String user_alias=null;
    private String keystoreFile=null;
    private String keystorePass=null;
    
    /*For ProcessSet*/
    ProcessSet processSet;
    String processfile="";


    /**
     * Creates a new ADEBSession.
     * @param l
     */
    public ADEBSession(ADEBLayer l) {
        super(l);
    }

    /**
     * Initializes the session using the parameters given in the XML configuration.
     * Possible parameters:
     * 
     * @param params The parameters given in the XML configuration.
     */
    public void init(SessionProperties params) {

        /*SSL stuff*/
        this.ssl=params.getProperty("ssl").equals("true") ? true : false;
        if(this.ssl)
        {
            System.out.println("SSL Mode");
        this.user_alias=params.getProperty("user_alias");
        this.keystoreFile=params.getProperty("keystore_file");
        this.keystorePass=params.getProperty("passphrase");
        }
        else 
        {
            System.out.println("TCP Mode");
        }
        
        
        //Initialize the parameters for the algorithm
        sentecho = false;
        delivered = false;

        this.rank = Integer.parseInt(params.getProperty("rank"));
        
        
        /*ProcessSet Stuff*/
        this.processfile = params.getProperty("processfile");
        this.processSet = ProcessSet.buildProcessSet(processfile,rank);


            N = this.processSet.getSize();

            echos = new ArrayList<String>(N);
            for(int i=0;i<N;i++)
                echos.add(BOTTOM);
            
            readys = new ArrayList<String>(N);
            for(int i=0;i<N;i++)
                readys.add(BOTTOM);

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
        else if (ev instanceof ReadyEvent)
            handleReadyEvent((ReadyEvent) ev);
        else
            try {
                ev.go();
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }
    }

    private void handleReadyEvent(ReadyEvent ev) {
        
        
        if (ev.getDir() == Direction.UP){

            String signature = ev.getMessage().popString();
            String alias = ev.getMessage().popString();
            
            Message message = ev.getMessage();

            int msg_rank = message.popInt();
            String recvd_msg = message.popString();
            
            //do stuff here
            System.out.println("[READY_RECEIVED] Source:"+msg_rank + 
                                " MESSAGE: "+ recvd_msg);


            if(readys.get(msg_rank).equals(BOTTOM))
            {
                readys.set(msg_rank, recvd_msg);
                System.out.println("Ready collected from process_"+msg_rank + ": "+recvd_msg);
            }

            String msg = checkMajority_f(readys);
            if(msg!=null && sentready == false) {
                sentready = true;
                multicastReadyEvent(msg, "[READY2]");
            }
           
            
            
            String msg2 = checkMajority_2f(readys);
            if(msg!=null && delivered == false) {
                delivered = true;
                Deliver(msg2, this.sender_rank);

            }
           

        }
        
        
    }

    private String checkMajority_f(ArrayList<String> readys) {

        int msgCount = 0;
        for(int i=0;i<readys.size();i++) {
            String current = readys.get(i);
            if (current == BOTTOM) {
                continue;
            }
            else {
                for(int j=0;j<readys.size();j++) {
                    if (current.equals(readys.get(j))) {
                        msgCount++;
                    }
                }
                if (msgCount > f) {
                    return current; //this is the message
                }
            }
        }
        
        return null; //means msgCount is not greater than f
    }

    
    private String checkMajority_2f(ArrayList<String> readys) {

        int msgCount = 0;
        for(int i=0;i<readys.size();i++) {
            String current = readys.get(i);
            if (current == BOTTOM) {
                continue;
            }
            else {
                for(int j=0;j<readys.size();j++) {
                    if (current.equals(readys.get(j))) {
                        msgCount++;
                    }
                }
                if (msgCount > (2*f)) {
                    return current; //this is the message
                }
            }
        }
        
        return null; //means msgCount is not greater than f
    }
    
    private void handleEchoEvent(EchoEvent ev) {
        if (ev.getDir() == Direction.UP){

            String signature = ev.getMessage().popString();
            String alias = ev.getMessage().popString();
            
            Message message = ev.getMessage();

            int msg_rank = message.popInt();
            String recvd_msg = message.popString();


            if(echos.get(msg_rank).equals(BOTTOM))
            {
                echos.set(msg_rank, recvd_msg);
                System.out.println("Echo collected from process_"+msg_rank + ": "+recvd_msg);
            }

            
            String msg = checkMajority_Nf2(echos);

            if(msg!=null && sentready == false) {
                sentready = true;
                multicastReadyEvent(msg, "[READY]");
            }
           

        }

    }


    private void multicastReadyEvent(String recvd_msg, String debug_msg) {

            ReadyEvent re = new ReadyEvent();
            re.getMessage().pushString(recvd_msg);
            re.getMessage().pushInt(this.rank);
            
            re.setDir(Direction.DOWN);
            re.setSourceSession(this);
            re.setChannel(channel);
            re.dest = new AppiaMulticast(null,processSet.getAllSockets());

            try {
                
                re.init();
                re.go();
                System.out.println(debug_msg + " Multicasting");
                
            } catch (AppiaEventException e) {
                e.printStackTrace();
            }            

    }

    private void Deliver(String msg, int s_rank) {

        System.out.println("DELIVERED: \""+ msg + "\" SENDER: "+ s_rank);

    }

    /***/
    private String checkMajority_Nf2(ArrayList<String> echos) {

        
        for(int i=0;i<echos.size();i++) {
            int msgCount = 0;
            String current = echos.get(i);
            if (current == BOTTOM) {
                continue;
            }
            else {
                for(int j=0;j<echos.size();j++) {
                    if (current.equals(echos.get(j))) {
                        msgCount++;
                    }
                }
                if (msgCount > (N+f)/2) {
//                    System.out.println("majority echos collected:"+msgCount+" for message:"+current);
                    return current; //this is the message
                }
            }
        }
        
        return null; //means msgCount is not greater than f
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
            
            sentecho = true;
            this.sender_rank = ev.getMessage().popInt();

                EchoEvent ee = new EchoEvent();
                final Message messageSend = ee.getMessage();
                String myString = ev.getMessage().popString();
                ev.getMessage().pushString(myString);
                messageSend.pushString(myString);
                ee.getMessage().pushInt(this.rank);
                ee.dest = new AppiaMulticast(null,processSet.getAllSockets());

                try {
                    //System.out.println("[EchoEvents] Multicasting");
                    ee.setSourceSession(this);
                    ee.setChannel(channel);
                    ee.setDir(Direction.DOWN);
                    ee.init();
                    ee.go();
                } catch (AppiaEventException e) {
                    e.printStackTrace();
                }            

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
            e.printStackTrace();
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
            if(this.ssl) 
            {
                System.out.println("Creating SSL socket on port:"+selflocal.getPort() + " file:" + keystoreFile);
                System.out.println("Multiple sockets required?");
                rse=new SslRegisterSocketEvent(channel,Direction.DOWN,this,selflocal.getPort(), keystoreFile,keystorePass.toCharArray());
            }
            else
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
            
            multicastSendEvent(ev.getText(), "[SEND]");
           
        }
//        else {
//            // Event is going UP
//            ev.setText(message.popString()); //try commenting this line and see if this still works
//            final long now = time.currentTimeMillis();
//            System.out.print("\n[BROACAST WEIRD] On ["+new Date(now)+"] : "+ev.getText()+"\n> ");
//        }
    }
    

    private void multicastSendEvent(String recvd_msg, String debug_msg) {

        SendEvent se = new SendEvent();
        final Message messageSend = se.getMessage();
        messageSend.pushString(recvd_msg);
        messageSend.pushInt(this.rank); //pushing the initiators rank
        se.dest = new AppiaMulticast(null,processSet.getAllSockets());
        try {
            se.setSourceSession(this);
            se.setChannel(channel);
            se.setDir(Direction.DOWN);
            se.init();
            se.go();
        } catch (AppiaEventException e) {
            e.printStackTrace();
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
