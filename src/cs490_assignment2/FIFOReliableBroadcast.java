package cs490_assignment2;

public interface FIFOReliableBroadcast {
	public void init(Process currentProcess, BroadcastReceiver br);
	public void addMember(Process member);
	public void removeMember(Process member);
	public void resetGroup();
	public void FIFObroadcast(Message m);
}
