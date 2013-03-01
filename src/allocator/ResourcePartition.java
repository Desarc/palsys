package allocator;

import java.io.Serializable;
import java.util.ArrayList;

public interface ResourcePartition extends Serializable {

	public IPAddress getNewLease(String requesterID, long validTime);

	public IPAddress getExistingLease(String ownerID);

	public IPAddress renewLease(String address, long extraTime);

	public ArrayList<IPAddress> reclaimExpiredLeases();

	public boolean isAssigned(String address);

	public void addFreeAddress(IPAddress address);

	public void addAssignedAddress(IPAddress address);

	public void removeAddress(IPAddress address);

	public int getFreeAddresses();

	public int getAssignedAddresses();
	
	public int getAddresses();

	public String getServerID();
	
	public ArrayList<IPAddress> reassignAddresses(int n);
	
	public void addMultipleAddresses(ArrayList<IPAddress> addresses);

	public void updateView(IPAddress view, int code);
	
	public ArrayList<IPAddress> getAllLeases();
	
	public boolean controls(String address);

}