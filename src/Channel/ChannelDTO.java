// Saving data to the Channel table

package Channel;

public class ChannelDTO {
	
	private int ID;
	private String CHANNEL_NAME;
	private String REGDATE;
	private String OWNER_ID;
	
	public String getOWNER_ID() {
		return OWNER_ID;
	}
	public void setOWNER_ID(String oWNER_ID) {
		OWNER_ID = oWNER_ID;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getCHANNEL_NAME() {
		return CHANNEL_NAME;
	}
	public void setCHANNEL_NAME(String cHANNEL_NAME) {
		CHANNEL_NAME = cHANNEL_NAME;
	}
	public String getREGDATE() {
		return REGDATE;
	}
	public void setREGDATE(String rEGDATE) {
		REGDATE = rEGDATE;
	}

}
