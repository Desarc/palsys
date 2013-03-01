package allocator;

import java.io.Serializable;

public class IPAddress implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String address;
	private long expireTime;
	private String currentOwnerID;
	private String controllerID;
	
	public IPAddress() {
		this.expireTime = -1;
	}
	
	public IPAddress(String address, long expireTime, String controllerID) {
		this.address = address;
		this.expireTime = expireTime;
		this.controllerID = controllerID;
	}
	
	public IPAddress(String address, long expireTime, String controllerID, String ownerID) {
		this.address = address;
		this.expireTime = expireTime;
		this.controllerID = controllerID;
		this.currentOwnerID = ownerID;
	}
	
	public boolean hasExpired() {
		return (expireTime < System.currentTimeMillis());
	}
	
	public void extendValidTime(long extraTime) {
		expireTime = System.currentTimeMillis()+extraTime;
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
	
	public String toString() {
		return address+"~"+expireTime+"~"+currentOwnerID+"~"+controllerID;
	}
}
