package allocator;

import java.util.ArrayList;

import gmi.GroupProxy;

public class AddressClient {

	private String clientID;
	private String groupname = "servergroup90";
	private GroupProxy groupProxy;
	private String address = "localhost";
	private boolean running;
	private ArrayList<IPAddress> leaseList;
	private ExternalAddressListener server;

	public AddressClient(String name, int port) {
		groupProxy = new GroupProxy(this, name, port, groupname, address); 
		server = (ExternalAddressListener) groupProxy.getServer();    
		leaseList = new ArrayList<IPAddress>();
		clientID = groupProxy.getIdentifier();
	}

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

		AddressClient client = new AddressClient(connName, port);
		while (true) {
			client.runningClient();
			System.out.println("RESTARTING CLIENT.");
		}
	}

	private void runningClient() {
		leaseList = new ArrayList<IPAddress>();
		running = true;
		for (int i = 0; i < 10; i++) {
			IPAddress lease = null;
			try {
				lease = server.requestAddress(clientID);
			} catch (NullPointerException e) {
				System.out.println("Server error.");
				e.printStackTrace();
			}
			if (lease == null) {
				System.out.println("Unable to get lease from server.");
			}
			else {
				leaseList.add(lease);
				System.out.println("IPAddress: "+lease.getAddress()+" from "+lease.getControllerID());
			}
		}
		int runs = 0;
		while(running){
			runs++;
			try {
				Thread.sleep(AddressPool.defaultValidTime/2);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (leaseList.size() != 0) {
				int n = (int)(Math.random()*leaseList.size()-1);
				n++;
				System.out.println("ATTEMPTING TO RENEW "+n+" LEASES.");
				ArrayList<IPAddress> failedList = new ArrayList<IPAddress>();
				for (int i = 0; i < n; i++) {
					IPAddress lease = leaseList.get(i);
					IPAddress renewed = null;
					try {
						//for (int j = 0; j < 3; j++) {
							renewed = server.renewLease(lease.getAddress());
						/*	if (renewed != null) {
								break;
							}
						}*/
					}	catch (Exception e) {
						System.out.println("Server error.");
						e.printStackTrace();
						renewed = null;
						failedList.add(lease);
					}
					if (renewed != null) {
						System.out.println("Renewed lease "+renewed.getAddress()+" from "+renewed.getControllerID());
					}
					else {
						failedList.add(lease);
						System.out.println("Failed to renew lease "+lease.getAddress()+".");
					}
				}
				for (IPAddress failed : failedList) {
					leaseList.remove(failed);
				}
			}
			else {
				running = false;
			}
			if (runs > 6) {
				running = false;
			}
		}
	}

	private static void usage() {
		System.out.println("Usage Client :: Client -c <clientname> -p <port>");
		System.exit(1);
	}

}
