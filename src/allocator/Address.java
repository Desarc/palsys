package allocator;

import gmi.ExternalGMIListener;

public interface Address extends ExternalGMIListener {
	
	public IPAddress requestAddress(String clientID);

}
