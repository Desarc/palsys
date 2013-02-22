package allocator;

import java.util.ArrayList;

import gmi.GroupProxy;

public class AddressClient {
	
	private String clientID;
	private String groupname = "servergroup1";
    private GroupProxy groupProxy;
    private String address = "localhost";
	private boolean running;
	private ArrayList<IPAddress> addressList;
    
public AddressClient(String name, int port) {
        clientID = name;
        groupProxy = new GroupProxy(this, name, port, groupname, address); 
        ExternalAddressListener server = (ExternalAddressListener) groupProxy.getServer();    
        IPAddress address = server.requestAddress(clientID);
        for(int i = 0; i<10; i++){
        	IPAddress addr = server.requestAddress(clientID);
        	addressList.add(addr);
        }
        running = true;
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
		//new HelloClient(connName, port); 
	}
	
	private void runningClient(){
		while(running){
			try {
				Thread.sleep(AddressPool.defaultValidTime/2);
				// TODO: Request new leas
			} catch (Exception e) {
				// TODO: handle exception. Server crash?
			}
		}
	}
	
	private static void usage() {
    	System.out.println("Usage Client :: Client -c <clientname> -p <port>");
    	System.exit(1);
    }
    
}
