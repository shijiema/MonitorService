package a.ma;

import java.net.InetAddress;
import java.net.Socket;

public class ServiceChecker  {

	public static ServiceStatus getServiceStatus(Service svc) throws Exception {
		
		if (svc == null) throw new Exception("No service is specified.");
		
		Socket aClient=null;
	    try {
	    	aClient = new Socket(InetAddress.getByName(svc.getIP()),svc.getPort());
	    	aClient.close();
	    	aClient = null;
	    	return ServiceStatus.UP;
	    }
	    catch (Exception e) {
	    	return ServiceStatus.DOWN;
	    }
	}
}
