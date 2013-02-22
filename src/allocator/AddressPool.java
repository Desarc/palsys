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
public class AddressPool implements ResourcePool {
	
	public static final long defaultValidTime = 60000;
	
	private ArrayList<ResourcePartition> activePartitions;
	private ArrayList<ResourcePartition> backupPartitions;	//inactive
	
	
	/**
	 * 
	 * @param addressBase format: "xxx.xxx.xxx."
	 */
	public AddressPool(String addressBase, ArrayList<ResourcePartition> controllers) {
		activePartitions = new ArrayList<ResourcePartition>();
		backupPartitions = new ArrayList<ResourcePartition>();
		
		for (ResourcePartition controller : controllers) {
			activePartitions.add(controller);
		}
		
		int c = activePartitions.size()-1;
		for(int i=254; i>0; i--) {
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
		for (ResourcePartition partition : activePartitions) {
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
		for (ResourcePartition partition : activePartitions) {
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
		for (ResourcePartition partition : activePartitions) {
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
		for (ResourcePartition partition : activePartitions) {
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
		for (ResourcePartition partition : activePartitions) {
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
	private void changeController(ResourcePartition oldController, ResourcePartition newController) {
		newController.addMultipleAddresses(oldController.getFreeAddresses());
		newController.addMultipleAddresses(oldController.getAssignedAddresses());
	}
	
	/**
	 * Example handling of partition crash.
	 * @param partition
	 */
	public void serverCrash(ResourcePartition partition) {
		if (activePartitions.contains(partition)) {
			activePartitions.remove(partition);
			if (backupPartitions.size() > 0) {
				ResourcePartition backup = backupPartitions.get(0);
				backupPartitions.remove(backup);
				changeController(partition, backup);
				activePartitions.add(backup);
			}
			else if (activePartitions.size() > 0) {
				ResourcePartition backup = activePartitions.get(0);
				changeController(partition, backup);
			}
			
		}
		else if (backupPartitions.contains(partition)) {
			backupPartitions.remove(partition);
		}
	}
	
	public int numberOfActiveServers() {
		return activePartitions.size();
	}
	
	public boolean isActive(ResourcePartition partition) {
		return activePartitions.contains(partition);
	}	
	
	public ArrayList<ResourcePartition> getActivePartitions() {
		return activePartitions;
	}
	
	/**
	 * TODO
	 */
	public void addServer(ResourcePartition partition) {
		for (ResourcePartition p : activePartitions) {
			partition.addMultipleAddresses(p.reassignAddresses(254/activePartitions.size()*(activePartitions.size()+1)));
		}
		activePartitions.add(partition);
		
	}

	public boolean addBackup(ResourcePartition partition) {
		if (!backupPartitions.contains(partition)) {
			backupPartitions.add(partition);
			return true;
		}
		return false;
	}

	public boolean removeBackup(ResourcePartition partition) {
		if (backupPartitions.contains(partition)) {
			backupPartitions.remove(partition);
			return true;
		}
		return false;
	}
}
