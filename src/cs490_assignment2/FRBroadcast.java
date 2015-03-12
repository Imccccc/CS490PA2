package cs490_assignment2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import cs490_assignment2.Message;

public class FRBroadcast implements FIFOReliableBroadcast{
	private HashMap<String, Process> group;
	private static HashSet<Message> deliveredSet;
	private static HashMap<String, Integer> lastSeqMap;
	private static ConcurrentHashMap<String, PriorityQueue<Message>> pendingMap;
	int numThread = 20;
	final ExecutorService executorService = Executors.newFixedThreadPool(numThread);

	@Override
	public void init(final Process currentProcess, final BroadcastReceiver br) {
		group = new HashMap<>();
		deliveredSet = new HashSet<>();
		lastSeqMap = new HashMap<>();
		pendingMap = new ConcurrentHashMap<>();
		
		Thread listener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ServerSocket serverSocket;
				try{
					serverSocket = new ServerSocket(currentProcess.getPort());
	    			while(true){			
	    				Socket receiveSocket = serverSocket.accept();
	    				//System.out.println("after accept");
	    				executorService.execute(new Runnable() {
							Socket receiveSocket;  					
							@Override
							public void run() {
								try{
				    				ObjectInputStream inStream = new ObjectInputStream(receiveSocket.getInputStream());
				    				Message receiveMessage = (Message) inStream.readObject();
				    				
				    				if(!deliveredSet.contains(receiveMessage)){
				    					//System.out.println("Checking");
				    					deliveredSet.add(receiveMessage);
				    					
				    					String[] info = receiveMessage.getMessageContents().split("\\|");
				    					String senderName = info[0];
				    					
				    					if(receiveMessage.getMessageNumber() == 0 || (lastSeqMap.get(senderName)!=null &&
				    						receiveMessage.getMessageNumber() == (lastSeqMap.get(senderName)+1))){
				    						// Should frDeliver the message, update the map
				    						lastSeqMap.put(senderName, receiveMessage.getMessageNumber());
				    						br.reveive(receiveMessage);
				    						FIFObroadcast(receiveMessage);
				    						
				    						if(pendingMap.containsKey(senderName)){
				    							// checking the pending map and deliver all messages that should be delivered
				    							PriorityQueue<Message> pq = pendingMap.get(senderName);
				    							while(pq.peek()!=null && pq.peek().getMessageNumber() == (lastSeqMap.get(senderName)+1)){
				    								receiveMessage = pq.poll();
				    								lastSeqMap.put(senderName, receiveMessage.getMessageNumber());
				    	    						br.reveive(receiveMessage);
				    	    						FIFObroadcast(receiveMessage);
				    							}
				    						}
				    						
				    					}
				    					else{
				    						// Put message into the pending map
				    						if(!pendingMap.containsKey(senderName)){
				    							// Need to initialize a priority queue
				    							pendingMap.put(senderName, new PriorityQueue<Message>());
				    						}
				    						pendingMap.get(senderName).add(receiveMessage);
				    					}
				    				}
				    				receiveSocket.close();		
								}
								catch(Exception e){
									e.printStackTrace();
								}
							}							
							public Runnable init(Socket receiveSocket){
								this.receiveSocket = receiveSocket;
								return this;
							}
						}.init(receiveSocket));
	    			}	    			
	    		}
	    		catch (Exception e){
	    			e.printStackTrace();
	    		}
				
			}
		});		

		listener.start();
	}

	@Override
	public void addMember(Process member) {
		group.put(member.getID(), member);
	}

	@Override
	public void removeMember(Process member) {
		group.remove(member.getID());
	}

	@Override
	public void resetGroup(){
		group = new HashMap<>();
	}
	
	@Override
	public void FIFObroadcast(Message message) {
		Socket sendSocket;
		ObjectOutputStream outputStream;
		try{
			for(Process member : group.values()){
				sendSocket = new Socket(member.getIP(), member.getPort());
				outputStream = new ObjectOutputStream(sendSocket.getOutputStream());
				outputStream.writeObject(message);
				outputStream.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
