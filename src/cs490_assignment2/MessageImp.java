package cs490_assignment2;

public class MessageImp implements Message, Comparable<Message>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7907285853581637610L;
	String contents;
	int number;
	
	public MessageImp(String s){
		this.contents = new String(s);
		this.number = -1;
	}
	
	public MessageImp(String s, int n){
		this.contents = new String(s);
		this.number = n;
	}
	
	@Override
	public int getMessageNumber() {
		return number;
	}

	@Override
	public void setMessageNumber(int messageNumber) {
		this.number = messageNumber;
	}

	@Override
	public String getMessageContents() {
		return contents;
	}

	@Override
	public void setMessageContents(String contents) {
		this.contents = new String(contents);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Message))
			return false;	
		if (obj == this)
			return true;
		return this.contents.equals(((Message) obj).getMessageContents()) && this.number==((Message) obj).getMessageNumber() ;
	}
 
	@Override
	public int hashCode(){
		return this.contents.length();//for simplicity reason
	}
	
    @Override
    public int compareTo(Message o) {
        return Integer.valueOf(this.number).compareTo(o.getMessageNumber());
    }
}
