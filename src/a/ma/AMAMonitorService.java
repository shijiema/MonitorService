package a.ma;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * no logging framework is used no command line parsing is implemented. please
 * simple invoke it without any parameter or an integer parameter as service's
 * TCP port. default port is 7777 if not parameter specified
 * 
 *
 */
public class AMAMonitorService {
	List<Service> serviceList = Collections.synchronizedList(new LinkedList<Service>());
	private Map<String, ServiceMonitor> serviceMonitors = new HashMap<String, ServiceMonitor>();
	

	// demonWareTelnet;demonwareWeb;gWeb;
	public String getServiceUnderMonitoring() {
		String serviceUnderMonitoring = "";
		for (Service svc : serviceList)
			serviceUnderMonitoring = serviceUnderMonitoring + svc.getName()
					+ ";";
		return serviceUnderMonitoring;
	}

	/**
	 * Use less static methods and attributes in order to use itself as a
	 * service to support unit and integration testing.
	 * 
	 * @param args
	 */
	public void registerServiceToMonitor(Service svc){
		if(serviceList.contains(svc))
			System.out.println(svc+" is already registered");
		else{
			serviceList.add(svc);
			serviceMonitors.put(svc.getName(), new ServiceMonitor(svc));
		}
	}
	public void prepareService(){
		// these should be read from configurable data sources such as Java
		// property files, xml files or database
		// eg. in property file, these can be defined as service1=ip1:port1
		// it's hard coded here for purpose of saving time so that I can focus
		// on more important things to demonstrate.
		serviceList.add(new Service("GRWeb", "www.globalrelay.com", 80));
		serviceList
				.add(new Service("demonWareTelnet", "www.demonware.com", 23));
		serviceList.add(new Service("demonwareWeb", "www.demonware.com", 80));
		serviceList.add(new Service("gWeb", "www.google.ca", 80));
		//this is for integration testing. hard coded for prototype purpose
		serviceList.add(new Service("monitorService", "localhost", 7779));

		// initiate service monitors
		for (Service svc : serviceList)
			serviceMonitors.put(svc.getName(), new ServiceMonitor(svc));
	}
	public void startService(String[] args) {
		ServerSocket AMAService = null;

		// default port is 7777
		try {

			AMAService = new ServerSocket(args.length == 0 ? 7777
					: Integer.parseInt(args[0]));
		} catch (Exception e) {
			System.out
					.println("Failed to start AMA Java Service Monitor Service");
			e.printStackTrace();
		}
		System.out.println("Service started...");
		// provide service
		while (true) {
			ClientSession cs;
			try {

				cs = new ClientSession(AMAService.accept(), this);
				cs.start();
			} catch (IOException e) {
				System.out
						.println("Failed to start service for client request");
				System.exit(-1);
			}
		}

	}

	public Map<String, ServiceMonitor> getServiceMonitors() {
		return serviceMonitors;
	}

	public static void main(String[] args) {
	
		AMAMonitorService mainService = new AMAMonitorService();
		mainService.prepareService();
		mainService.startService(args);
	}
}
