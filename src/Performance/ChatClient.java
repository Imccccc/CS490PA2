package Performance;

import java.io.*;
import java.net.*;
import java.util.*;

import cs490_assignment2.*;
import cs490_assignment2.Process;



public class ChatClient implements BroadcastReceiver{
    static Socket sendSocket;
    static String contact;
    static ReliableBroadcast rBroadcast;
    static FIFOReliableBroadcast frBroadcast;
    static boolean isFIFO = false;
    static boolean senderflag = false;
    
    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int serverPort = 1234;
        int heartbeat_rate = 1000;
        int clientPort = 0;
       
        
        if(args.length == 1){
        	clientPort = Integer.parseInt(args[0]);
        }
        else if(args.length > 1){
        	for(String s : args){
        		if(s.equals("-s")){
        			senderflag = true;
            		System.out.println("Sender flag is on");
        		}else if (s.equals("-f")) {
                	isFIFO = true;
            		System.out.println("FIFO flag is on");
				}else{
		        	clientPort = Integer.parseInt(s);
				}
        	}
        }
        else{            
        	System.err.println("Usage: java ChatClient [-f] [-s] <port number>");
        	System.exit(1);
        }
        
        Timer timer = new Timer();

        Socket echoSocket;	// Socket used to connect with server
        PrintWriter out;
        BufferedReader in;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("What is your User name?");
        
        String userInput;
        String username;
        String selfIPString = "localhost";
        username = stdIn.readLine();
        assert(username.isEmpty()==false);
        String serverMessage;
        
        try{
            /*register client */
            echoSocket = new Socket(hostName, serverPort);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
            out.println("REGISTER|"+username+","+clientPort);
            System.out.println("Waiting server response.....");
            
            serverMessage = in.readLine();
            if(serverMessage!=null && serverMessage.equals("ERROR")){ // check the server state
                System.err.println("Username already be registed by other User!");
                System.exit(1);
            }
            else{
            	// Server will return the IP if registration success
            	selfIPString = serverMessage;
            }
            System.out.println("After register");

            if(isFIFO){
            	frBroadcast = new FRBroadcast();
            	frBroadcast.init(new Process(selfIPString,  clientPort, username), new ChatClient());
            }
            else{
            	rBroadcast = new RBBroadcast();
            	rBroadcast.init(new Process(selfIPString,  clientPort, username), new ChatClient());
            }
            
            System.out.println("After Initialization");
            // Use timer to send heartbeat every heartbeat_rate milliseconds
            TimerTask task = new TimerTask_heartbeat(out, in);
            timer.schedule(task, 0, (heartbeat_rate-100)); 
            
            int seq = 0;
            if (senderflag) {
            	Thread.sleep(3000);
	            for (int i=0; i < 10000; i++) {
	            	//Thread.sleep(50);
	                Message m = new MessageImp(username+"| this is message No."+i, seq++);
	                if(isFIFO){
	                	frBroadcast.FIFObroadcast(m);
	                	if (i % 100 == 0) {
							System.out.println("i is "+i);
						}
	               	}
	               	else{
	               		rBroadcast.rbroadcast(m);
	               		if (i % 100 == 0) {
							System.out.println("i is "+i);
						}
	               	}
	                	//System.out.println(m.getMessageContents());
	            }	
			}
            while(true){
            	if(senderflag){
            		break;
            	}
            }
        } catch (Exception e) {
//            System.err.println("Couldn't get I/O for the connection to " +
//                               hostName);
        	e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void sendGetRequest(PrintWriter out, BufferedReader in, boolean isPrint) throws IOException{
        String serverMessage;
        
        out.println("GET|List group");
        while((serverMessage = in.readLine())!= null){
            //System.out.println(serverMessage);
            String[] r_info = serverMessage.split("\\|");
            if(isFIFO){
            	frBroadcast.resetGroup();
            }
            else{
            	rBroadcast.resetGroup();
            }
            if(isPrint){
                System.out.println("there are "+r_info.length+" processs");
                System.out.println("Username\t  "+"IP adress\t"+"Port");
            }
            for(String temp : r_info){
                //System.out.println(temp);
                String m_info[] = temp.split(",");
                String temp1[] = m_info[1].split("/");
                String temp2[] = temp1[1].split(":");
                m_info[1] = new String(temp2[0]);
                if(isPrint)
                    System.out.println(m_info[0]+"\t  "+ m_info[1]+"\t"+m_info[2]);
                if(isFIFO){
                	frBroadcast.addMember(new Process(m_info[1], Integer.parseInt(m_info[2]), m_info[0]));
                }
                else{
                	rBroadcast.addMember(new Process(m_info[1], Integer.parseInt(m_info[2]), m_info[0]));
                }
            }
        }
        if(isPrint)
            System.out.println("**********Group get Done**************\n");
    }
    

    
    static class TimerTask_heartbeat extends TimerTask{
    	PrintWriter out;
        BufferedReader in;
        String username;
        
        public TimerTask_heartbeat(PrintWriter out, BufferedReader in){
            this.out = out;
            this.in = in;
        }
        
        @Override
        public void run() {
            try{
                synchronized (out){
                	out.println("heartbeat|"+username);
                	sendGetRequest(out, in, false);
                }
            }
            catch(Exception e){
            	System.out.println("Heartbeat Exception");
            	e.printStackTrace();
            }
        }
        
    }
    
    static class inChannelHandler extends Thread{
    	BufferedReader in;
    	public inChannelHandler(BufferedReader in){
    		this.in = in;
    	}
    	
    }

	@Override
	public void reveive(Message m) {
		String info[] = m.getMessageContents().split("\\|"); //String format username|content
		System.out.println(info[0] + ": " +info[1]);		
	}

}