package test;

import java.util.ArrayList;

import allocator.AddressPool;
import allocator.AddressPartition;
import allocator.IPAddress;
import allocator.ResourcePartition;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AddressPoolTest extends TestCase {
	
	String base = "192.168.1.";
	String testID = "testID";
	
	AddressPartition server1;
	AddressPartition server2;
	AddressPartition server3;
	AddressPartition backup1;
	AddressPartition backup2;
	
	ArrayList<ResourcePartition> servers;
	AddressPool pool;

	public void setUp() {
		servers = new ArrayList<ResourcePartition>();
		server1 = new AddressPartition("testServer1");
		servers.add(server1);
		pool = new AddressPool(base, servers);
	}
	
	public void setUpMultiple() {
		servers = new ArrayList<ResourcePartition>();
		server1 = new AddressPartition("testServer1");
		server2 = new AddressPartition("testServer2");
		server3 = new AddressPartition("testServer3");
		servers.add(server1);
		servers.add(server2);
		servers.add(server3);
		pool = new AddressPool(base, servers);
	}
	
	public void setUpBackups() {
		setUpMultiple();
		backup1 = new AddressPartition("backup1");
		backup2 = new AddressPartition("backup2");
		pool.addBackup(backup1);
		pool.addBackup(backup2);
	}
	
	public void testGetLease() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000, server1.getServerID());
		assertEquals(testID, lease.getOwner());
		assertEquals(base+1, lease.getAddress());
		assertEquals(false, lease.hasExpired());
		assertEquals(lease.getControllerID(), server1.getServerID());
	}
	
	public void testLeaseExpired() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000, server1.getServerID());
		try {
			Thread.sleep(2200);
		} catch (InterruptedException e) {
			fail();
		}
		assertEquals(true, lease.hasExpired());
	}
	
	public void testRenewLease() {
		setUp();
		IPAddress lease = pool.getNewLease(testID, 2000, server1.getServerID());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			fail();
		}
		pool.renewLease(testID, 2000, server1.getServerID());
		assertEquals(false, lease.hasExpired());
	}
	
	public void testLoadBalancing() {
		setUpMultiple();
		int total = 0;
		for (ResourcePartition server : pool.getActivePartitions()) {
			assertTrue(server.getFreeAddresses().size() > 0);
			total += server.getFreeAddresses().size();
		}
		assertEquals(254, total);
	}
	
	public void testServerCrash() {
		setUpBackups();
		pool.serverCrash(server2);
		assertEquals(3, pool.numberOfActiveServers());
		assertFalse(pool.isActive(server2));
		int total = 0;
		for (ResourcePartition server : pool.getActivePartitions()) {
			assertTrue(server.getFreeAddresses().size() > 0);
			total += server.getFreeAddresses().size();
		}
		assertEquals(254, total);
		pool.serverCrash(server3);
		assertEquals(3, pool.numberOfActiveServers());
		assertFalse(pool.isActive(server3));
		total = 0;
		for (ResourcePartition server : pool.getActivePartitions()) {
			assertTrue(server.getFreeAddresses().size() > 0);
			total += server.getFreeAddresses().size();
		}
		assertEquals(254, total);
		pool.serverCrash(backup1);
		assertEquals(2, pool.numberOfActiveServers());
		assertFalse(pool.isActive(backup1));
		total = 0;
		for (ResourcePartition server : pool.getActivePartitions()) {
			assertTrue(server.getFreeAddresses().size() > 0);
			total += server.getFreeAddresses().size();
		}
		assertEquals(254, total);
	}
	
	
	
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for allocator");
		suite.addTestSuite(AddressPoolTest.class);
		return suite;
	}

}
