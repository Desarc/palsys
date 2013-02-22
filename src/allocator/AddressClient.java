package allocator;

import gmi.GroupProxy;
import hello.HelloClient;

public class AddressClient {
	
	private String clientID;
	private String groupname = "servergroup1";
    private GroupProxy groupProxy;
    private String address = "localhost";
    
public AddressClient(String name, int port) {
        clientID = name;
        groupProxy = new GroupProxy(this, name, port, groupname, address); 
        Address server = (Address) groupProxy.getServer();    
        IPAddress address = server.requestAddress(clientID);
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
            
		new HelloClient(connName, port); 
	}
	
	private static void usage() {
    	System.out.println("Usage Client :: Client -c <clientname> -p <port>");
    	System.exit(1);
    }
    
}
