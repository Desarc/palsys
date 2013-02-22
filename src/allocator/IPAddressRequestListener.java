package allocator;

import gmi.ExternalGMIListener;

public interface IPAddressRequestListener extends ExternalGMIListener {
	
	public IPAddress requestAddress(String clientID);

}
