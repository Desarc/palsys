package allocator;

import java.util.ArrayList;


public class AddressPartition implements ResourcePartition {
	
	private static final long serialVersionUID = 1L;
	
	private final int GETNEW = 0;
	private final int GETEXISTING = 1;
	private final int RENEW = 2;
	private final int RECLAIM = 3;
	private final int ADDFREE = 4;
	private final int ADDASSIGNED = 5;
	private final int REMOVE = 6;
	private final int REASSIGN = 7;
	private final int ADDMULTIPLE = 8;

	private String serverID;
	
	private ArrayList<IPAddress> free;
	private ArrayList<IPAddress> assigned;
	
	public AddressPartition(String serverID) {
		this.serverID = serverID;
		free = new ArrayList<IPAddress>();
		assigned = new ArrayList<IPAddress>();
	}
	
	private ArrayList<IPAddress> modifyPartition(int code) {
		return modifyPartition(code, "");
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, int n) {
		return modifyPartition(code, "", 0, "", null, null, n);
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, ArrayList<IPAddress> leases) {
		return modifyPartition(code, "", 0, "", null, leases, 0);
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, IPAddress newLease) {
		return modifyPartition(code, "", 0, "", newLease, null, 0);
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, String clientID) {
		return modifyPartition(code, clientID, 0);
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, String clientID, long validTime) {
		return modifyPartition(code, clientID, validTime, "");
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, long validTime, String address) {
		return modifyPartition(code, "", validTime, address);
	}
	
	private ArrayList<IPAddress> modifyPartition(int code, String clientID, long validTime, String address) {
		return modifyPartition(code, clientID, validTime, address, null, null, 0);
	}

	private synchronized ArrayList<IPAddress> modifyPartition(int code, String clientID, long validTime, String address, IPAddress newLease, 
			ArrayList<IPAddress> leases, int n) {
		ArrayList<IPAddress> returnValue = new ArrayList<IPAddress>();
		switch (code) {
		case GETNEW:
			if (free.size() > 0) {
				IPAddress lease = free.get(free.size()-1);
				lease.assign(clientID, validTime);
				free.remove(lease);
				assigned.add(lease);
				returnValue.add(lease);
			}
			else {
				returnValue = null;
			}
			break;
		case GETEXISTING:
			boolean found = false;
			for (IPAddress lease : assigned) {
				if (lease.getOwner().equals(clientID)) {
					returnValue.add(lease);
					found = true;
				}
			}
			if (!found) {
				returnValue = null;
			}
			break;
		case RENEW:
			boolean success = false;
			for (IPAddress lease : assigned) {
				if (lease.getAddress().equals(address)) {
					lease.extendValidTime(validTime);
					returnValue.add(lease);
					success = true;
				}
			}
			if (!success) {
				returnValue = null;
				System.out.println("Failed to renew lease "+address+": expired!");
			}
			break;
		case RECLAIM:
			ArrayList<IPAddress> expired = new ArrayList<IPAddress>();
			for (IPAddress lease : assigned) {
				if (lease.hasExpired()) {
					lease.setFree();
					if (!freeContains(lease)) {
						free.add(lease);
					}
					expired.add(lease);
				}
			}
			for (IPAddress lease : expired) {
				assigned.remove(lease);
			}
			returnValue = expired;
			break;
		case ADDFREE:
			newLease.setController(serverID);
			if (!freeContains(newLease)) {
				free.add(newLease);
			}
			if (assignedContains(newLease)) {
				removeAssigned(newLease);
			}
			returnValue = null;
			break;
		case ADDASSIGNED:
			newLease.setController(serverID);
			if (!assignedContains(newLease)) {
				assigned.add(newLease);
			}
			if (freeContains(newLease)) {
				removeFree(newLease);
			}
			returnValue = null;
			break;
		case REMOVE:
			if (freeContains(newLease)) {
				removeFree(newLease);
			}
			if (assignedContains(newLease)) {
				removeAssigned(newLease);
			}
			returnValue = null;
			break;
		case REASSIGN:
			ArrayList<IPAddress> addresses = new ArrayList<IPAddress>();
			int count = 0;
			for (int i = 0; i < n; i++) {
				if (free.size() == 0 || count >= n) {
					break;
				}
				count++;
				addresses.add(free.get(0));
				free.remove(0);
			}
			for (int j = 0; j < n; j++) {
				if (assigned.size() == 0 || count >= n) {
					break;
				}
				count++;
				addresses.add(assigned.get(0));
				assigned.remove(0);
			}
			returnValue = addresses;
			break;
		case ADDMULTIPLE:
			for (IPAddress lease : leases) {
				lease.setController(serverID);
				System.out.println("Reassigning "+lease.getAddress()+" to "+serverID);
				if (lease.hasExpired()) {
					if (!freeContains(lease)) {
						free.add(lease);
					}
					if (assignedContains(lease)) {
						removeAssigned(lease);
					}
				}
				else {
					if (!assignedContains(lease)) {
						assigned.add(lease);
					}
					if (freeContains(lease)) {
						removeFree(lease);
					}
				}
			}
			returnValue = null;
			break;
		default:
			returnValue = null;
			break;
		}
		return returnValue;	
	}
	
	public IPAddress getNewLease(String requesterID, long validTime) {
		ArrayList<IPAddress> returnValue = modifyPartition(GETNEW, requesterID, validTime);
		if (returnValue == null || returnValue.size() == 0) {
			return null;
		}
		return returnValue.get(0);
	}
	
	public IPAddress getExistingLease(String ownerID) {
		ArrayList<IPAddress> returnValue = modifyPartition(GETEXISTING, ownerID);
		if (returnValue == null || returnValue.size() == 0) {
			return null;
		}
		return returnValue.get(0);
	}
	
	public IPAddress renewLease(String address, long extraTime) {
		ArrayList<IPAddress> returnValue = modifyPartition(RENEW, extraTime, address);
		if (returnValue == null || returnValue.size() == 0) {
			return null;
		}
		return returnValue.get(0);
	}
	
	public ArrayList<IPAddress> reclaimExpiredLeases() {
		return modifyPartition(RECLAIM);
	}
	
	public boolean isAssigned(String address) {
		for (IPAddress lease : assigned) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	public void addFreeAddress(IPAddress address) {
		modifyPartition(ADDFREE, address);
	}
	
	public void addAssignedAddress(IPAddress address) {
		modifyPartition(ADDASSIGNED, address);
	}

	public void removeAddress(IPAddress address) {
		modifyPartition(REMOVE, address);
	}
	
	public int getFreeAddresses() {
		return free.size();
	}
	
	public int getAssignedAddresses() {
		return assigned.size();
	}
	
	public int getAddresses() {
		return getAssignedAddresses()+getFreeAddresses();
	}
	
	public String getServerID() {
		return serverID;
	}

	public ArrayList<IPAddress> reassignAddresses(int n) {
		return modifyPartition(REASSIGN, n);
	}

	public void addMultipleAddresses(ArrayList<IPAddress> addresses) {
		modifyPartition(ADDMULTIPLE, addresses);
	}

	public void updateView(IPAddress view, int code) {
		switch(code) {
		case AddressPool.ASSIGNED:
			System.out.println(view.getAddress()+" assigned by "+view.getControllerID()+" to "+view.getOwner());
			addAssignedAddress(view);
			break;
		case AddressPool.EXPIRED:
			System.out.println(view.getAddress()+" reclaimed by "+view.getControllerID());
			addFreeAddress(view);
			break;
		case AddressPool.RENEWED:
			System.out.println(view.getAddress()+" renewed by "+view.getControllerID()+" to "+view.getOwner());
			renewLease(view.getAddress(), AddressPool.defaultValidTime);
			break;
		case AddressPool.NEWSERVER:
			System.out.println("Initial view update: "+view.getAddress()+" controlled by "+view.getControllerID());
			if (view.hasExpired()) {
				addFreeAddress(view);
			}
			else {
				addAssignedAddress(view);
			}
		}
	}


	public ArrayList<IPAddress> getAllLeases() {
		ArrayList<IPAddress> leases = new ArrayList<IPAddress>();
		leases.addAll(free);
		leases.addAll(assigned);
		return leases;
	}

	private boolean freeContains(IPAddress lease) {
		for (IPAddress freeLease : free) {
			if(freeLease.getAddress().equals(lease.getAddress())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean assignedContains(IPAddress lease) {
		for (IPAddress assignedLease : assigned) {
			if(assignedLease.getAddress().equals(lease.getAddress())) {
				return true;
			}
		}
		return false;
	}

	private void removeFree(IPAddress lease) {
		for (int i = 0; i < free.size(); i++) {
			if(free.get(i).getAddress().equals(lease.getAddress())) {
				free.remove(i);
				return;
			}
		}
	}
	
	private void removeAssigned(IPAddress lease) {
		for (int i = 0; i < assigned.size(); i++) {
			if(assigned.get(i).getAddress().equals(lease.getAddress())) {
				assigned.remove(i);
				return;
			}
		}
	}
	
	public boolean controls(String address) {
		for (IPAddress lease : free) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		for (IPAddress lease : assigned) {
			if (lease.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
}
