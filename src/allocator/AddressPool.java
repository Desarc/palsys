package allocator;

import java.util.ArrayList;


/**
 * Class for managing an address pool. The pool is split between the active servers,
 * and each of the active servers must have an identical instance of this class.
 * For now, the AddressServer is not the real AddressServer program, but rather a
 * placeholder class for keeping track of which addresses are managed by who.
 * Every active server must know this.
 * 
 * @author desarc
 *
 */
public class AddressPool {

	private ArrayList<IPAddress> free;
	private ArrayList<IPAddress> assigned;
	
	private ArrayList<AddressServer> activeServers;
	private AddressServer currentBackup;
	
	private long defaultValidTime = 60000;
	
	/**
	 * 
	 * @param addressBase format: "xxx.xxx.xxx."
	 */
	public AddressPool(String addressBase, ArrayList<AddressServer> controllers) {
		free = new ArrayList<IPAddress>();
		assigned = new ArrayList<IPAddress>();
		activeServers = new ArrayList<AddressServer>();
		
		currentBackup = controllers.get(0);
		for (AddressServer controller : controllers) {
			activeServers.add(controller);
		}
		
		int c = 0;
		//TODO: better load balancing
		for(int i=254; i>0; i--) {
			//Alternating between servers to ensure load balancing.
			IPAddress address = new IPAddress(addressBase+i, -1, activeServers.get(c)); 
			free.add(address);
			activeServers.get(c).addManagedAddress(address);
			c++;
			if (c >= activeServers.size()) {
				c = 0;
			}
		}
	}
	
	public IPAddress getNewLease(String requesterID) {
		return getNewLease(requesterID, defaultValidTime);
	}
	
	/**	TODO: check for duplicate requesterIDs
	 * Method for getting a new lease.
	 * 
	 * @param requesterID
	 * @param validTime
	 * @return
	 */
	public IPAddress getNewLease(String requesterID, long validTime) {
		IPAddress lease = free.get(free.size()-1);
		lease.assign(requesterID, validTime);
		free.remove(lease);
		assigned.add(lease);
		return lease;
	}
	
	/**
	 * Method for getting an existing lease.
	 * 
	 * @param ownerID
	 * @return
	 */
	public IPAddress getExistingLease(String ownerID) {
		for (IPAddress lease : assigned) {
			if (lease.getOwner().equals(ownerID)) {
				return lease;
			}
		}
		//if requesting client has no active lease, assign a new one
		return getNewLease(ownerID, defaultValidTime);		
	}
	
	public boolean renewLease(String ownerID) {
		return renewLease(ownerID, defaultValidTime);
	}
	
	/**
	 * Method for renewing an active lease.
	 * 
	 * @param ownerID
	 * @param extraTime
	 * @return
	 */
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
	
	/**
	 * Checks if any assigned leases have expired, and if so,
	 * moves them to the free pool and notifies the previous owner.
	 */
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
	
	/**
	 * Checking if a specific address is already assigned.
	 * 
	 * @param address
	 * @return
	 */
	public boolean isAssigned(String address) {
		for (IPAddress lease : assigned) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * 
	 * @param addresses
	 * @param newController
	 */
	public void changeController(ArrayList<IPAddress> addresses, AddressServer newController) {
		for (IPAddress address : addresses) {
			address.setController(newController);
		}
	}
	
	
	/**TODO
	 * Method to try and notify the owner of the address.
	 * Assuming each client can only have 1 active lease.
	 * 
	 * @param lease
	 */
	private void notifyReclaim(String previousOwner) {
		
	}
	
	/**
	 * Example handling of server crash.
	 * @param server
	 */
	public void serverCrash(AddressServer server) {
		if (activeServers.contains(server)) {
			changeController(server.getManagedAddresses(), currentBackup);
		}
	}
}
