package allocator;

import java.io.Serializable;
import java.util.ArrayList;

public interface ResourcePool extends Serializable {
	
	public IPAddress getNewLease(String requesterID, String partitionID);
	
	public IPAddress getExistingLease(String ownerID, String partitionID);
	
	public IPAddress renewLease(String address);
	
	public void serverCrash(String serverID);
	
	public ArrayList<IPAddress> reclaimExpiredLeases(String serverID);
	
	public boolean isAssigned(String address);
	
	public int numberOfActiveServers();
	
	public boolean isActive(ResourcePartition partition);
	
	public void addServer(ResourcePartition partition, boolean first);
	
	public boolean addBackup(ResourcePartition partition);
	
	public boolean removeBackup(ResourcePartition partition);
	
	public String getCreatedBy();

	public void setCreatedBy(String createdBy);

	public void mergeView(IPAddress view, int code);	
	
	public ArrayList<IPAddress> getAllLeases();
	
	public int getAddresses();
	
}
