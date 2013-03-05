package allocator;

import java.util.ArrayList;
import java.util.Vector;

import gmi.MembershipListener;
import gmi.ServerSideProxy;
import gmi.View;
import gmi.protocols.Anycast;
import gmi.protocols.Multicast;

public class AddressServer implements MembershipListener, ExternalAddressListener, InternalAddressListener {

	private static final long serialVersionUID = 1L;
	
	private final int poolSize = 20;
	private final String addressBase = "192.168.1.";

	private String serverID;

	private String groupname = "servergroup90";
	private String address = "localhost";

	private ServerSideProxy proxy;
	private InternalAddressListener internalListener;
	private ResourcePool addressPoolView;
	private Vector<String> groupView;

	private int renewSuccess = 0;
	private IPAddress lease;

	public static void main(String[] arg) {
		String connName = null;
		int port = 0;
		try {
			for (int i = 0 ; i < arg.length ; i += 2) {
				if (arg[i].equals("-c")) {
					connName = arg[i+1];
				} else if (arg[i].equals("-p")) {
					port = Integer.parseInt(arg[i+1]);
				} else {
					usage();
				}
			}
		}
		catch (Exception e) {
			usage();
		}
		AddressServer server = new AddressServer(connName, port); 
		server.runServer();
		
	}

	private void runServer() {
		while (true) {
			try {
				Thread.sleep(5000);
				reclaimExpiredLeases();
				printStatus();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printStatus() {
		ArrayList<IPAddress> leases = addressPoolView.getAllLeases();
		System.out.println("===================================================================\n");
		for (IPAddress lease : leases) {
			System.out.println(lease.getAddress()+" controlled by "+lease.getControllerID()+", leased to "+lease.getOwner());
		}
		System.out.println("\n===================================================================");
	}
		
	private static void usage() {
		System.out.println("Usage Server :: server -c <srvname> -p <port>");
		System.exit(1);
	}

	public AddressServer(String name, int port) {
		groupView = new Vector<String>();

		addressPoolView = new AddressPool(addressBase, poolSize);

		proxy = new ServerSideProxy(this, port, name, address);
		proxy.join(groupname);
		internalListener = (InternalAddressListener) proxy.getInternalStub(InternalAddressListener.class);
		this.serverID = proxy.getIdentifier();
		System.out.println("ServerID: "+serverID);
	}

	public void reclaimExpiredLeases() {
		ArrayList<IPAddress> expired = addressPoolView.reclaimExpiredLeases(serverID);
		for (IPAddress address : expired) {
			System.out.println("Reclaimed "+address.getAddress());
			internalListener.updateView(address.toString(), AddressPool.EXPIRED);
		}
	}


	@Anycast
	public IPAddress requestAddress(String clientID) {
		IPAddress address = addressPoolView.getNewLease(clientID, serverID);
		if (address != null) {
			System.out.println("Assigned address "+address.getAddress()+" to "+clientID);
			internalListener.updateView(address.toString(), AddressPool.ASSIGNED);
		}
		else {
			reclaimExpiredLeases();
		}
		return address;
	}

	public Object success() {
		return renewSuccess;
	}
	
	public Object lease() {
		return lease.toString();
	}

	public void ViewChange(View view) {
		Vector<String> newView = view.getView();
		boolean first = false;
		if (groupView.size() == 0 && newView.size() == 1) {
			first = true;
		}
		//if there are new servers in the view, add to datastructure
		for (String server : newView) {
			if (!groupView.contains(server)) {
				groupView.add(server);
				addressPoolView.addServer(new AddressPartition(server), first);
			}
		}
		//update the new server with the datastructure to ensure consistency
		for (String server : newView) {
			if (!serverID.equals(server) && !first) {
				ArrayList<IPAddress> leases = addressPoolView.getAllLeases();
				for (IPAddress lease : leases) {
					internalListener.updateView(lease.toString(), AddressPool.NEWSERVER);
				}
			}
		}
		//if servers have disappeared from the view, remove from datastructure
		ArrayList<String> toBeRemoved = new ArrayList<String>();
		for (String server : groupView) {
			if (!newView.contains(server)) {
				toBeRemoved.add(server);
			}
		}
		for (String server : toBeRemoved) {
			groupView.remove(server);
			addressPoolView.serverCrash(server);
		}
	}
	
	@Multicast
	public void updateView(Object view, int code) {
		IPAddress changedView = toIPAddress(view.toString());
		if (code == AddressPool.NEWSERVER) {
			if (addressPoolView.getAddresses() == poolSize) {
				return;
			}
			addressPoolView.mergeView(changedView, code);
		}
		else if (!changedView.getControllerID().equals(serverID)) {
			addressPoolView.mergeView(changedView, code);
		}
	}

	@Multicast
	public IPAddress renewLease(String address) {
		boolean success = false;
		IPAddress lease = null;
		try {
			lease = addressPoolView.renewLease(address, serverID);
		} catch (Exception e) {
			System.out.println("Crash on renew");
			e.printStackTrace();
		}
		this.lease = lease;
		if (lease != null) {
			renewSuccess = 1;
		}
		else {
			renewSuccess = 0;
		}
		Object[] status = new Object[0];
		try {
			status = (Object[])internalListener.success();
		} catch (Exception e) {
			System.out.println("Crash from internalListener.success()");
			e.printStackTrace();
		}
		for (int i = 0; i < status.length; i++) {
			if (status[i] != null && status[i].toString().equals("1")) {
				success = true;
			}
		}
		if (success) {
			Object[] leases = new Object[0];
			try {
				leases = (Object[])internalListener.lease();
			} catch(NullPointerException e) {
				System.out.println("Crash from internalListener.lease()");
				e.printStackTrace();
			}
			for (int i = 0; i < leases.length; i++) {
				IPAddress findLease = null;
				try {
					if (leases[i] != null) {
						findLease = toIPAddress(leases[i].toString());
					}
				} catch (Exception e) {
					System.out.println("Crash in findLease");
					e.printStackTrace();
				}
				try {
				if (findLease != null) {
					lease = findLease;
					if (findLease.getControllerID().equals(serverID)) {
						System.out.println("Renewed lease "+lease.getAddress()+" to "+lease.getOwner());
						internalListener.updateView(lease.toString(), AddressPool.RENEWED);
						break;
					}
				}
				} catch (Exception e) {
					System.out.println("Crash on updateView");
					e.printStackTrace();
				}
			}
		}
		return lease;
	}

	private IPAddress toIPAddress(String view) {
		if (view == null || view.length() == 0) {
			return null;
		}
		try {
			int ind1 = view.indexOf("~");
			int ind2 = view.indexOf("~", ind1+1);
			int ind3 = view.indexOf("~", ind2+1);
			String address = view.substring(0,ind1);
			String expireTime = view.substring(ind1+1, ind2);
			String owner = view.substring(ind2+1, ind3);
			String controller = view.substring(ind3+1);
			return new IPAddress(address, Long.parseLong(expireTime), controller, owner);
		} catch (Exception e) {
			return null;
		}
	}

}
