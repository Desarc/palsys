package allocator;

import java.util.ArrayList;


/**
 * Class for managing an address pool. The pool is split between the active partitions,
 * and each of the active partitions must have an identical instance of this class.
 * Each active partition is assigned an address partition. This class represents a view
 * of the total address pool.
 * 
 * @author desarc
 *
 */
public class AddressPool implements ResourcePool {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final long defaultValidTime = 12000;
	
	public static final int ASSIGNED = 0;
	public static final int RENEWED = 1;
	public static final int EXPIRED = 2;
	public static final int NEWSERVER = 3;
	
	private int poolSize;

	private ArrayList<ResourcePartition> activePartitions;
	private ArrayList<ResourcePartition> backupPartitions;	//inactive
	
	private String addressBase;
	
	private String createdBy;
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	
	
	
	/**
	 * 
	 * @param addressBase format: "xxx.xxx.xxx."
	 */
	public AddressPool(String addressBase, int poolSize) {
		activePartitions = new ArrayList<ResourcePartition>();
		backupPartitions = new ArrayList<ResourcePartition>();
		this.addressBase = addressBase;
		this.poolSize = poolSize;
	}
	
	public IPAddress getNewLease(String requesterID, String partitionID) {
		return getNewLease(requesterID, defaultValidTime, partitionID);
	}
	
	/** Method for getting a new lease.
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
	
	public IPAddress renewLease(String address, String serverID) {
		return renewLease(address, defaultValidTime, serverID);
	}
	
	/**
	 * Method for renewing an active lease.
	 * 
	 * @param ownerID
	 * @param extraTime
	 * @return
	 */
	public IPAddress renewLease(String address, long extraTime, String serverID) {
		for (ResourcePartition partition : activePartitions) {
			if (partition.controls(address) && partition.getServerID().equals(serverID)) {
				return partition.renewLease(address, extraTime);
			}
		}
		//return null if unable to renew lease
		return null;
	}
	
	/**
	 * Checks if any assigned leases have expired, and if so,
	 * moves them to the free pool and notifies the previous owner.
	 */
	public ArrayList<IPAddress> reclaimExpiredLeases(String serverID) {
		ArrayList<IPAddress> expired = new ArrayList<IPAddress>();
		for (ResourcePartition partition : activePartitions) {
			if (partition.getServerID().equals(serverID)) {
				expired.addAll(partition.reclaimExpiredLeases());
			}
		}
		return expired;
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
	
	private void changeController(ResourcePartition oldController, ResourcePartition newController) {
		newController.addMultipleAddresses(oldController.reassignAddresses(oldController.getAddresses()));
	}
	
	public void serverCrash(String serverID) {
		for (ResourcePartition partition : activePartitions) {
			if (partition.getServerID().equals(serverID)) {
				System.out.println("Handling server crash.");
				if (activePartitions.contains(partition)) {
					System.out.println("Removing server from pool: "+partition.getServerID());
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
	
	public void addServer(ResourcePartition partition, boolean first) {
		if (first) {
			for(int i=poolSize; i>0; i--) {
				IPAddress address = new IPAddress(addressBase+i, -1, partition.getServerID()); 
				partition.addFreeAddress(address);
			}
		}
		else {
			for (ResourcePartition p : activePartitions) {
				partition.addMultipleAddresses(p.reassignAddresses(poolSize/(activePartitions.size()*(activePartitions.size()+1))));
			}
		}
		System.out.println("Adding server to pool: "+partition.getServerID());
		activePartitions.add(partition);
	}
	
	public void mergeView(IPAddress view, int code) {
		for (ResourcePartition partition : activePartitions) {
			if (partition.getServerID().equals(view.getControllerID())) {
				partition.updateView(view, code);
			}
		}
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

	public ArrayList<IPAddress> getAllLeases() {
		ArrayList<IPAddress> leases = new ArrayList<IPAddress>();
		for (ResourcePartition partition : activePartitions) {
			leases.addAll(partition.getAllLeases());
		}
		return leases;
	}

	public int getAddresses() {
		int n = 0;
		for (ResourcePartition partition : activePartitions) {
			n += partition.getAddresses();
		}
		return n;
	}
	
	public boolean controls(String address, String serverID) {
		for (ResourcePartition partition : activePartitions) {
			if (partition.getServerID().equals(serverID)) {
				return partition.controls(address);
			}
		}
		return false;
	}
}
