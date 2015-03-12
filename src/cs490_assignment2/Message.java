package cs490_assignment2;

import java.io.Serializable;

public interface Message extends Serializable{
	public int getMessageNumber();
	public void setMessageNumber(int messageNumber);
	public String getMessageContents();
	public void setMessageContents(String contents);
}
