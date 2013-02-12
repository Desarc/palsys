package allocator;

import java.util.ArrayList;

public class AddressServer {

	private String ServerID;
	
	private ArrayList<IPAddress> managedAddresses;
	
	public AddressServer(String serverID) {
		this.ServerID = serverID;
		managedAddresses = new ArrayList<IPAddress>();
	}
	
	public void addManagedAddress(IPAddress address) {
		managedAddresses.add(address);
	}
	
	public void removeManagedAddress(IPAddress address) {
		managedAddresses.remove(address);
	}
	
	public ArrayList<IPAddress> getManagedAddresses() {
		return managedAddresses;
	}
}
