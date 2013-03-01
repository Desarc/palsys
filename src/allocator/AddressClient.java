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

		running = true;
		clientID = groupProxy.getIdentifier();
		runningClient();
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

		new AddressClient(connName, port);
	}

	private void runningClient(){
		for (int i = 0; i < 10; i++) {
			IPAddress lease = server.requestAddress(clientID);
			if (lease == null) {
				System.out.println("Unable to get new lease from server.");
				break;
			}
			leaseList.add(lease);
			System.out.println("IPAddress: "+lease.getAddress()+" from "+lease.getControllerID());
		}
		while(running){
			try {
				Thread.sleep(AddressPool.defaultValidTime/2);
				if (leaseList.size() != 0) {
					//int n = (int)(Math.random()*leaseList.size());
					//for (int i = 0; i < n; i++) {
						//IPAddress lease = leaseList.get(i);
						IPAddress lease = leaseList.get(0);
						System.out.println("Attempting to renew lease from "+lease.getControllerID());
						IPAddress renewed = server.renewLease(lease.getAddress());
						if (renewed != null) {
							System.out.println("Renewed lease "+renewed.getAddress()+" from "+renewed.getControllerID());
						}
						else {
							leaseList.remove(lease);
							System.out.println("Failed to renew lease.");
						}
					//}
				}
			} catch (Exception e) {
				System.out.println("Crash?");
				e.printStackTrace();
			}
		}
	}

	private static void usage() {
		System.out.println("Usage Client :: Client -c <clientname> -p <port>");
		System.exit(1);
	}

}
