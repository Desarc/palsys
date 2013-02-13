package allocator;

public class IPAddress {
	
	private String address;
	private long expireTime;
	private String currentOwnerID;
	private String controllerID;
	
	public IPAddress(String address, long expireTime, String controllerID) {
		this.address = address;
		this.expireTime = expireTime;
		this.controllerID = controllerID;
	}
	
	public boolean hasExpired() {
		return (expireTime < System.currentTimeMillis());
	}
	
	public void extendValidTime(long extraTime) {
		expireTime += extraTime;
	}
	
	public void setFree() {
		expireTime = -1;
		currentOwnerID = null;
	}
	
	public void assign(String newOwner, long validTime) {
		currentOwnerID = newOwner;
		expireTime = System.currentTimeMillis() + validTime;
	}
	
	public void setController(String controllerID) {
		this.controllerID = controllerID;
	}
	
	public String getOwner() {
		return this.currentOwnerID;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getControllerID() {
		return this.controllerID;
	}
}
