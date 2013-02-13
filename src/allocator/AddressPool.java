package allocator;

import java.util.ArrayList;


/**
 * Class for managing an address pool. The pool is split between the active partitions,
 * and each of the active partitions must have an identical instance of this class.
 * Each active partition is assigned an address partition.
 * 
 * @author desarc
 *
 */
public class AddressPool {
	
	public static final long defaultValidTime = 60000;
	
	private ArrayList<AddressPartition> activePartitions;
	private AddressPartition currentBackup;
	
	
	/**
	 * 
	 * @param addressBase format: "xxx.xxx.xxx."
	 */
	public AddressPool(String addressBase, ArrayList<AddressPartition> controllers) {
		activePartitions = new ArrayList<AddressPartition>();
		
		currentBackup = controllers.get(0);
		for (AddressPartition controller : controllers) {
			activePartitions.add(controller);
		}
		
		int c = activePartitions.size()-1;
		for(int i=254; i>0; i--) {
			//Alternating between partitions to ensure load balancing.
			IPAddress address = new IPAddress(addressBase+i, -1, activePartitions.get(c).getServerID()); 
			activePartitions.get(c).addFreeAddress(address);
			if (i < c*254/activePartitions.size()) {
				c--;
			}
		}
	}
	
	public IPAddress getNewLease(String requesterID, String partitionID) {
		return getNewLease(requesterID, defaultValidTime, partitionID);
	}
	
	/**	TODO: check for duplicate requesterIDs
	 * Method for getting a new lease.
	 * 
	 * @param requesterID
	 * @param validTime
	 * @return
	 */
	public IPAddress getNewLease(String requesterID, long validTime, String partitionID) {
		for (AddressPartition partition : activePartitions) {
			if (partition.getServerID().equals(partitionID)) {
				return partition.getNewLease(requesterID, validTime);
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
	public IPAddress getExistingLease(String ownerID, String partitionID) {
		for (AddressPartition partition : activePartitions) {
			if (partition.getServerID().equals(partitionID)) {
				return partition.getExistingLease(ownerID);
			}
		}
		return null;
	}
	
	public boolean renewLease(String ownerID, String partitionID) {
		return renewLease(ownerID, defaultValidTime, partitionID);
	}
	
	/**
	 * Method for renewing an active lease.
	 * 
	 * @param ownerID
	 * @param extraTime
	 * @return
	 */
	public boolean renewLease(String ownerID, long extraTime, String partitionID) {
		for (AddressPartition partition : activePartitions) {
			if (partition.getServerID().equals(partitionID)) {
				return partition.renewLease(ownerID, extraTime);
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
		for (AddressPartition partition : activePartitions) {
			partition.reclaimExpiredLeases();
		}
	}
	
	/**
	 * Checking if a specific address is already assigned.
	 * 
	 * @param address
	 * @return
	 */
	public boolean isAssigned(String address) {
		for (AddressPartition partition : activePartitions) {
			if (partition.isAssigned(address)) {
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
	private void changeController(AddressPartition oldController, AddressPartition newController) {
		for (IPAddress address : oldController.getFreeAddresses()) {
			address.setController(newController.getServerID());
			newController.addFreeAddress(address);
		}
		for (IPAddress address : oldController.getAssignedAddresses()) {
			address.setController(newController.getServerID());
			newController.addAssignedAddress(address);
		}
	}
	
	/**
	 * Example handling of partition crash.
	 * @param partition
	 */
	public void serverCrash(AddressPartition partition) {
		if (activePartitions.contains(partition)) {
			if (partition == currentBackup) {
				if (!assignNewBackup(partition)) {
					return;
				}
			}
			changeController(partition, currentBackup);
			activePartitions.remove(partition);
		}
	}
	
	/**
	 * Method for changing which partition takes over in case of a crash
	 * @param oldBackup
	 * @return
	 */
	private boolean assignNewBackup(AddressPartition oldBackup) {
		for (AddressPartition partition : activePartitions) {
			if (partition != oldBackup) {
				currentBackup = partition;
				return true;
			}
		}
		//return false if no other backup partition is available
		return false;
	}
	
	public int numberOfActiveServers() {
		return activePartitions.size();
	}
	
	public boolean isActive(AddressPartition partition) {
		return activePartitions.contains(partition);
	}
	
	public AddressPartition getCurrentBackup() {
		return currentBackup;
	}
	
	public ArrayList<AddressPartition> getActivePartitions() {
		return activePartitions;
	}
}
