package test.ma;

import static org.junit.Assert.*;

import org.junit.Test;

import a.ma.Service;
import a.ma.ServiceChecker;
import a.ma.ServiceStatus;

public class ServiceCheckerTest {

	@Test
	public void testNullService() {
		try {
			ServiceChecker.getServiceStatus(new Service("nullService",null, 1234));
		} catch (Exception e) {
			// it's better to define messages as constants
			assertTrue("No service is specified.".equals(e.getMessage()));
		}
	}
	@Test
	public void testValidServiceUp() {
		ServiceStatus status=null;
		try {
			status = ServiceChecker.getServiceStatus(new Service("googleWeb","google.ca", 80));
		} catch (Exception e) {
			assertFalse("No service is specified.".equals(e.getMessage()));
		}
		assertTrue(status!=null);
		assertTrue(status==ServiceStatus.UP);
	}
	/**
	 * usually the local echo service is not running. please make sure echo service is not running
	 * before running this test
	 */
	@Test
	public void testValidServiceDown() {
		ServiceStatus status=null;
		try {
			status = ServiceChecker.getServiceStatus(new Service("localEcho","localhost", 7));
		} catch (Exception e) {
			assertFalse("No service is specified.".equals(e.getMessage()));
		}
		assertTrue(status!=null);
		assertTrue(status==ServiceStatus.DOWN);
	}
//you know this is not up
	@Test
	public void testValidServiceDown2() {
		ServiceStatus status=null;
		try {
			status = ServiceChecker.getServiceStatus(new Service("demonwareTelnet","www.demonware.com", 23));
		} catch (Exception e) {
			assertFalse("No service is specified.".equals(e.getMessage()));
		}
		assertTrue(status!=null);
		assertTrue(status==ServiceStatus.DOWN);
	}	

}
