package allocator;

import java.util.ArrayList;

public class AddressPartition implements ResourcePartition {
	
	private String ServerID;
	
	private ArrayList<IPAddress> free;
	private ArrayList<IPAddress> assigned;
	
	public AddressPartition(String serverID) {
		this.ServerID = serverID;
		free = new ArrayList<IPAddress>();
		assigned = new ArrayList<IPAddress>();
	}
	
	public IPAddress getNewLease(String requesterID, long validTime) {
		IPAddress lease = free.get(free.size()-1);
		lease.assign(requesterID, validTime);
		free.remove(lease);
		assigned.add(lease);
		return lease;
	}
	
	public IPAddress getExistingLease(String ownerID) {
		for (IPAddress lease : assigned) {
			if (lease.getOwner().equals(ownerID)) {
				return lease;
			}
		}
		//if requesting client has no active lease, assign a new one
		return getNewLease(ownerID, AddressPool.defaultValidTime);		
	}
	
	public boolean renewLease(String ownerID, long extraTime) {
		for (IPAddress lease : assigned) {
			if (lease.getOwner().equals(ownerID)) {
				lease.extendValidTime(extraTime);
				return true;
			}
		}
		//return false if unable to renew lease
		return false;
	}
	
	public void reclaimExpiredLeases() {
		for (IPAddress lease : assigned) {
			if (lease.hasExpired()) {
				String previousOwner = lease.getOwner();
				lease.setFree();
				free.add(lease);
				assigned.remove(lease);
				notifyReclaim(previousOwner);
			}
		}
	}
	
	public boolean isAssigned(String address) {
		for (IPAddress lease : assigned) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	public void addFreeAddress(IPAddress address) {
		address.setController(ServerID);
		free.add(address);
		if (assigned.contains(address)) {
			assigned.remove(address);
		}
	}
	
	public void addAssignedAddress(IPAddress address) {
		address.setController(ServerID);
		assigned.add(address);
		if (free.contains(address)) {
			free.remove(address);
		}
	}

	public void removeAddress(IPAddress address) {
		if (free.contains(address)) {
			free.remove(address);
		}
		else if (assigned.contains(address)) {
			assigned.remove(address);
		}
	}
	
	public ArrayList<IPAddress> getFreeAddresses() {
		return free;
	}
	
	public ArrayList<IPAddress> getAssignedAddresses() {
		return assigned;
	}
	
	public String getServerID() {
		return ServerID;
	}
	
	/**TODO
	 * Method to try and notify the owner of the address.
	 * Assuming each client can only have 1 active lease.
	 * 
	 * @param lease
	 */
	private void notifyReclaim(String previousOwner) {
		
	}

	public ArrayList<IPAddress> reassignAddresses(int n) {
		ArrayList<IPAddress> addresses = new ArrayList<IPAddress>();
		for (int i = 0; i < n; i++) {
			if (free.size() == 0) {
				for (int j = i; j < n; i++) {
					if (assigned.size() == 0) {
						break;
					}
					addresses.add(assigned.get(0));
					assigned.remove(0);
				}
				break;
			}
			addresses.add(free.get(0));
			free.remove(0);
		}
			
		return addresses;
	}

	public void addMultipleAddresses(ArrayList<IPAddress> addresses) {
		for (IPAddress address : addresses) {
			address.setController(ServerID);
			if (address.hasExpired()) {
				free.add(address);
			}
			else {
				assigned.add(address);
			}
		}
		
	}
}
