package allocator;

import java.util.ArrayList;

public interface ResourcePartition {

	public abstract IPAddress getNewLease(String requesterID, long validTime);

	public abstract IPAddress getExistingLease(String ownerID);

	public abstract boolean renewLease(String ownerID, long extraTime);

	public abstract void reclaimExpiredLeases();

	public abstract boolean isAssigned(String address);

	public abstract void addFreeAddress(IPAddress address);

	public abstract void addAssignedAddress(IPAddress address);

	public abstract void removeAddress(IPAddress address);

	public abstract ArrayList<IPAddress> getFreeAddresses();

	public abstract ArrayList<IPAddress> getAssignedAddresses();

	public abstract String getServerID();

}