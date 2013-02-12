package test;

import java.util.ArrayList;

import allocator.AddressPool;
import allocator.AddressServer;
import allocator.IPAddress;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AddressPoolTest extends TestCase {
	
	String base = "192.168.1.";
	String testID = "testID";
	
	AddressServer server1;
	AddressServer server2;
	AddressServer server3;
	
	ArrayList<AddressServer> servers;
	AddressPool pool;

	public void setUp() {
		servers = new ArrayList<AddressServer>();
		server1 = new AddressServer("testServer1");
		servers.add(server1);
		pool = new AddressPool(base, servers);
	}
	
	public void setUpMultiple() {
		servers = new ArrayList<AddressServer>();
		server1 = new AddressServer("testServer1");
		server2 = new AddressServer("testServer2");
		server3 = new AddressServer("testServer3");
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);
		pool = new AddressPool(base, servers);
	}
	
	public void testGetLease() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000);
		assertEquals(testID, lease.getOwner());
		assertEquals(base+1, lease.getAddress());
		assertEquals(false, lease.hasExpired());
		assertEquals(server1, lease.getController());
	}
	
	public void testLeaseExpired() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000);
		try {
			Thread.sleep(2200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(true, lease.hasExpired());
	}
	
	public void testRenewLease() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pool.renewLease(testID, 2000);
		assertEquals(false, lease.hasExpired());
	}
	
	public void testLoadBalancing() {
		setUpMultiple();
		int total = 0;
		for (AddressServer server : servers) {
			assertTrue(server.getManagedAddresses().size() > 0);
			total += server.getManagedAddresses().size();
		}
		assertEquals(254, total);
	}
	
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for allocator");
		suite.addTestSuite(AddressPoolTest.class);
		return suite;
	}

}
