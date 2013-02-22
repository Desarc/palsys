package allocator;

import java.io.Serializable;
import java.util.ArrayList;

public interface ResourcePartition extends Serializable {

	public IPAddress getNewLease(String requesterID, long validTime);

	public IPAddress getExistingLease(String ownerID);

	public boolean renewLease(String ownerID, long extraTime);

	public void reclaimExpiredLeases();

	public boolean isAssigned(String address);

	public void addFreeAddress(IPAddress address);

	public void addAssignedAddress(IPAddress address);

	public void removeAddress(IPAddress address);

	public ArrayList<IPAddress> getFreeAddresses();

	public ArrayList<IPAddress> getAssignedAddresses();

	public String getServerID();
	
	public ArrayList<IPAddress> reassignAddresses(int n);
	
	public void addMultipleAddresses(ArrayList<IPAddress> addresses);

}