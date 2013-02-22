package allocator;

public interface ResourcePool {
	
	public IPAddress getNewLease(String requesterID, String partitionID);
	
	public IPAddress getExistingLease(String ownerID, String partitionID);
	
	public boolean renewLease(String ownerID, String partitionID);
	
	public void serverCrash(ResourcePartition partition);
	
	public void reclaimExpiredLeases();
	
	public boolean isAssigned(String address);
	
	public int numberOfActiveServers();
	
	public boolean isActive(ResourcePartition partition);
	
	public void addServer(ResourcePartition partition);
	
	public boolean addBackup(ResourcePartition partition);
	
	public boolean removeBackup(ResourcePartition partition);
	
}
