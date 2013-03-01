package allocator;

import gmi.InternalGMIListener;

public interface InternalAddressListener extends InternalGMIListener {
	
	public Object success();
	
	public Object lease();
	
	public void updateView(Object view, int code);

}
