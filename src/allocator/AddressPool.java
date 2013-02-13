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
	
	public static final long defaultValidTime = 60000;
	
	private ArrayList<AddressServer> activeServers;
	private AddressServer currentBackup;
	
	
	/**
	 * 
	 * @param addressBase format: "xxx.xxx.xxx."
	 */
	public AddressPool(String addressBase, ArrayList<AddressServer> controllers) {
		activeServers = new ArrayList<AddressServer>();
		
		currentBackup = controllers.get(0);
		for (AddressServer controller : controllers) {
			activeServers.add(controller);
		}
		
		int c = activeServers.size()-1;
		for(int i=254; i>0; i--) {
			//Alternating between servers to ensure load balancing.
			IPAddress address = new IPAddress(addressBase+i, -1, activeServers.get(c)); 
			activeServers.get(c).addFreeAddress(address);
			if (i < c*254/activeServers.size()) {
				c--;
			}
		}
	}
	
	public IPAddress getNewLease(String requesterID, String serverID) {
		return getNewLease(requesterID, defaultValidTime, serverID);
	}
	
	/**	TODO: check for duplicate requesterIDs
	 * Method for getting a new lease.
	 * 
	 * @param requesterID
	 * @param validTime
	 * @return
	 */
	public IPAddress getNewLease(String requesterID, long validTime, String serverID) {
		for (AddressServer server : activeServers) {
			if (server.getServerID().equals(serverID)) {
				return server.getNewLease(requesterID, validTime);
			}
		}
		return null;
	}
	
	/**
	 * Method for getting an existing lease.
	 * 
	 * @param ownerID
	 * @return
	 */
	public IPAddress getExistingLease(String ownerID, String serverID) {
		for (AddressServer server : activeServers) {
			if (server.getServerID().equals(serverID)) {
				return server.getExistingLease(ownerID);
			}
		}
		return null;
	}
	
	public boolean renewLease(String ownerID, String serverID) {
		return renewLease(ownerID, defaultValidTime, serverID);
	}
	
	/**
	 * Method for renewing an active lease.
	 * 
	 * @param ownerID
	 * @param extraTime
	 * @return
	 */
	public boolean renewLease(String ownerID, long extraTime, String serverID) {
		for (AddressServer server : activeServers) {
			if (server.getServerID().equals(serverID)) {
				return server.renewLease(ownerID, extraTime);
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
		for (AddressServer server : activeServers) {
			server.reclaimExpiredLeases();
		}
	}
	
	/**
	 * Checking if a specific address is already assigned.
	 * 
	 * @param address
	 * @return
	 */
	public boolean isAssigned(String address) {
		for (AddressServer server : activeServers) {
			if (server.isAssigned(address)) {
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
	private void changeController(AddressServer oldController, AddressServer newController) {
		for (IPAddress address : oldController.getFreeAddresses()) {
			address.setController(newController);
			newController.addFreeAddress(address);
		}
		for (IPAddress address : oldController.getAssignedAddresses()) {
			address.setController(newController);
			newController.addAssignedAddress(address);
		}
	}
	
	/**
	 * Example handling of server crash.
	 * @param server
	 */
	public void serverCrash(AddressServer server) {
		if (activeServers.contains(server)) {
			if (server == currentBackup) {
				if (!assignNewBackup(server)) {
					return;
				}
			}
			changeController(server, currentBackup);
			activeServers.remove(server);
		}
	}
	
	/**
	 * Method for changing which server takes over in case of a crash
	 * @param oldBackup
	 * @return
	 */
	private boolean assignNewBackup(AddressServer oldBackup) {
		for (AddressServer server : activeServers) {
			if (server != oldBackup) {
				currentBackup = server;
				return true;
			}
		}
		//return false if no other backup server is available
		return false;
	}
	
	public int numberOfActiveServers() {
		return activeServers.size();
	}
	
	public boolean isActive(AddressServer server) {
		return activeServers.contains(server);
	}
	
	public AddressServer getCurrentBackup() {
		return currentBackup;
	}
	
	public ArrayList<AddressServer> getActiveServers() {
		return activeServers;
	}
}
