package allocator;

public class IPAddress {
	
	private String address;
	private long expireTime;
	private String currentOwner;
	private AddressServer controller;
	
	public IPAddress(String address, long expireTime, AddressServer controller) {
		this.address = address;
		this.expireTime = expireTime;
		this.controller = controller;
	}
	
	public boolean hasExpired() {
		return (expireTime < System.currentTimeMillis());
	}
	
	public void extendValidTime(long extraTime) {
		expireTime += extraTime;
	}
	
	public void setFree() {
		expireTime = -1;
		currentOwner = null;
	}
	
	public void assign(String newOwner, long validTime) {
		currentOwner = newOwner;
		expireTime = System.currentTimeMillis() + validTime;
	}
	
	public void setController(AddressServer controller) {
		this.controller = controller;
	}
	
	public String getOwner() {
		return this.currentOwner;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public AddressServer getController() {
		return this.controller;
	}
}
