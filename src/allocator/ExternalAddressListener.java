package allocator;

import gmi.ExternalGMIListener;

public interface ExternalAddressListener extends ExternalGMIListener {
	
	public IPAddress requestAddress(String clientID);
	
	public IPAddress renewLease(String clientID);

}
