package cs490_assignment2;

public interface ReliableBroadcast {
	public void init(Process currentProcess, BroadcastReceiver br);
	public void addMember(Process member);
	public void removeMember(Process member);
	public void resetGroup();
	public void rbroadcast(Message m);
}
