package cs490_assignment2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RBBroadcast implements ReliableBroadcast{
	private HashMap<String, Process> group;
	private static HashSet<Message> deliveredSet;
	static int numThread = 20;
	public AtomicInteger i = new AtomicInteger(0);
	
	@Override
	public void init(final Process currentProcess, final BroadcastReceiver br) {
		group = new HashMap<>();
		deliveredSet = new HashSet<>();
		final ExecutorService executorService = Executors.newFixedThreadPool(numThread);
		//System.out.println("The listening is not running");
		
		Thread listener = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ServerSocket serverSocket;
				try{
					serverSocket = new ServerSocket(currentProcess.getPort());

	    			while(true){			
	    				Socket receiveSocket = serverSocket.accept();
	    				executorService.execute(new Runnable() {
							Socket receiveSocket;  					
							@Override
							public void run() {
								try{
									//System.out.println("after accept");
									ObjectInputStream inStream = new ObjectInputStream(receiveSocket.getInputStream());
									Message receiveMessage = (Message) inStream.readObject();

									if(!deliveredSet.contains(receiveMessage)){
										//System.out.println("Checking");
										deliveredSet.add(receiveMessage);
										br.reveive(receiveMessage);
										rbroadcast(receiveMessage);
										if (i.incrementAndGet() % 100 == 0) {
											System.out.println(currentProcess.getID() + " received: " + i.get());
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
		//System.out.println("The listening is running");
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
	public void rbroadcast(Message message) {
		Socket sendSocket;
		ObjectOutputStream outputStream;
		try{
			for(Process member : group.values()){
				sendSocket = new Socket(member.getIP(), member.getPort());
				outputStream = new ObjectOutputStream(sendSocket.getOutputStream());
				outputStream.writeObject(message);
				outputStream.close();
				sendSocket.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
