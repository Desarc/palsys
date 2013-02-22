package allocator;

import java.util.ArrayList;

import gmi.MembershipListener;
import gmi.ServerSideProxy;
import gmi.View;
import gmi.protocols.Anycast;

public class AddressServer implements MembershipListener, ExternalAddressListener, InternalAddressListener {

	private String serverID;
	
	private String groupname = "servergroup1";
    private String address = "localhost";
    
    private ServerSideProxy proxy;
    private InternalAddressListener internalListener;
    private ResourcePool addressPoolView;
	
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
        new AddressServer("server1", connName, port);       
        //thread sleep to avoid cpu utilization when they are waiting for any messages
        while (true) {
     	   try {
     		   Thread.sleep(100);
     	   } catch (InterruptedException e) {
     		   e.printStackTrace();
     	   }
        }
     }
     
     private static void usage() {
     	System.out.println("Usage Server :: server -c <srvname> -p <port>");
     	System.exit(1);
     }
     
     public AddressServer(String serverID, String name, int port) {
    	this.serverID = serverID;
    	
    	//TODO: fix this
    	ArrayList<ResourcePartition> servers = new ArrayList<ResourcePartition>();
    	servers.add(new AddressPartition(serverID));
    	addressPoolView = new AddressPool("192.168.1.", servers);
    	
     	proxy = new ServerSideProxy(this, port, name, address);
     	proxy.join(groupname);
     	internalListener = (InternalAddressListener) proxy.getInternalStub(InternalAddressListener.class);
     }
     
	@Anycast
	public IPAddress requestAddress(String arg) {
		IPAddress address = addressPoolView.getNewLease(proxy.getExternalIdentifier(), serverID);
		System.out.println("Assigned address "+address.getAddress()+" to "+proxy.getExternalIdentifier());
		return address;
	}
	
	public Object view() {
        return addressPoolView;
    }

	public void ViewChange(View view) {
		Object[] objs = (Object[]) internalListener.view();
        addressPoolView = (AddressPool)objs[0];
	}
	

}
