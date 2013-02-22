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
	
	@Override
	public IPAddress getNewLease(String requesterID, long validTime) {
		IPAddress lease = free.get(free.size()-1);
		lease.assign(requesterID, validTime);
		free.remove(lease);
		assigned.add(lease);
		return lease;
	}
	
	
	@Override
	public IPAddress getExistingLease(String ownerID) {
		for (IPAddress lease : assigned) {
			if (lease.getOwner().equals(ownerID)) {
				return lease;
			}
		}
		//if requesting client has no active lease, assign a new one
		return getNewLease(ownerID, AddressPool.defaultValidTime);		
	}
	
	@Override
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
	
	@Override
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
	
	@Override
	public boolean isAssigned(String address) {
		for (IPAddress lease : assigned) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addFreeAddress(IPAddress address) {
		free.add(address);
		if (assigned.contains(address)) {
			assigned.remove(address);
		}
	}
	
	@Override
	public void addAssignedAddress(IPAddress address) {
		assigned.add(address);
		if (free.contains(address)) {
			free.remove(address);
		}
	}

	@Override
	public void removeAddress(IPAddress address) {
		if (free.contains(address)) {
			free.remove(address);
		}
		else if (assigned.contains(address)) {
			assigned.remove(address);
		}
	}
	
	@Override
	public ArrayList<IPAddress> getFreeAddresses() {
		return free;
	}
	
	@Override
	public ArrayList<IPAddress> getAssignedAddresses() {
		return assigned;
	}
	
	@Override
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
}
