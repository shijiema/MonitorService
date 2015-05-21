package a.ma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * this is a class taking care of communication between subscriber and monitor
 * service. Monitor Service Communication Protocol: 
 * Basic format: 
 * command
 * [parameter] 
 * .end. 
 * Commands: 
 * 1. list service: 
 * 2. subscribe: 
 * [svc] 
 * 3. set grace period: 
 * [grace period in seconds] 
 * 4. set poll period: 
 * [poll period in seconds] 
 * 5. set outage period: 
 * [yyyy-MM-dd HH:mm:ss<->yyyy-MM-dd HH:mm:ss]
 * 6. register service:
 * name=host:port
 * .end.
 * @author ama
 *
 */
public class ClientSession extends Thread {
	private Socket subscriberSocket;
	private ServiceSubscriber subscriber;
	private BufferedReader input = null;
	private PrintWriter output = null;
	private String aLineFromClient = null;
	private AMAMonitorService aMonitorService = null;
	public ClientSession(Socket subscriberSocket, AMAMonitorService aService) throws IOException {
		this.aMonitorService = aService;
		this.subscriberSocket = subscriberSocket;
		subscriber = new ServiceSubscriber(subscriberSocket);
		try {
			input = new BufferedReader(new InputStreamReader(
					subscriberSocket.getInputStream()));
			output = new PrintWriter(subscriberSocket.getOutputStream(), true);
			//broad cast services under monitored
			output.println("ServiceList:");
			output.println(aService.getServiceUnderMonitoring());
			output.println(".end.");
		} catch (IOException e) {
			System.out
					.println("Failed to initialize input and output streams.");
			throw new IOException(
					"Failed to initialize input and output streams.");
			// if(subscriberSocket.isClosed())
			// return;
		}
	}
	private void readALine() throws IOException{
		aLineFromClient = input.readLine();
		System.out.println("Receiced From Client:"+subscriber+":"+aLineFromClient);
	}
//TODO: make a function for each command
	@Override
	public void run() {
		boolean  isProcessed = false;
		while (true) {
			try {
				readALine();
				// subscribe to services
				if (ServerCommand.SUBSCRIBE_SERVICE.toString().equals(aLineFromClient)) {
					isProcessed = true;
					readALine();
					while (!ServerCommand.END_COMMAND.toString().equals(
							aLineFromClient)) {

						try{
							//add subscriber to the service monitor as a subscription
							aMonitorService.getServiceMonitors().get(aLineFromClient)
						
								.addSubscriber(subscriber);
							//make adding subscription as part of servicemonitor's addSubscriber
							//subscriber.addSubscription(aMonitorService.getServiceMonitors().get(aLineFromClient).getServiceUnderMonitored());
						System.out.println("Added subscriber on "+aMonitorService.getServiceMonitors().get(aLineFromClient));
						}catch(Exception e){
							System.out.println("Failed to add subscription");
						}
						readALine();
					}

				}
				if (ServerCommand.REGISTER_SERVICE.toString().equals(aLineFromClient)) {
					isProcessed = true;
					readALine();
					while (!ServerCommand.END_COMMAND.toString().equals(
							aLineFromClient)) {

						try{
							//suppose value provided are under contract formatted as name=host:port
							String[] svcDefinition = aLineFromClient.split("=");
							String[] svcInfo = svcDefinition[1].split(":");
							Service newSvc = new Service(svcDefinition[0],svcInfo[0],Integer.parseInt(svcInfo[1]));
							//do some checking to make sure service definition is valid
							//then register it into service monitor registry
							aMonitorService.registerServiceToMonitor(newSvc);
							
						System.out.println("Registered service "+newSvc);
						}catch(Exception e){
							System.out.println("Failed to add subscription");
						}
						readALine();
					}

				}
				// set up grace period
				 if (ServerCommand.SET_GRACE_PERIOD.toString().equals(
						aLineFromClient)) {
					 isProcessed = true;
					 readALine();
					// if you send multiple grace periods, then only the last
					// one is taken
					while (!ServerCommand.END_COMMAND.toString().equals(
							aLineFromClient)) {
						subscriber.setGracePeriod(Integer
								.parseInt(aLineFromClient));
						readALine();
					}
				}
				// set up polling frequency
				 if (ServerCommand.SET_POLLING_PERIOD.toString().equals(
						aLineFromClient)) {
					 isProcessed = true;
					 readALine();
					// if you send multiple polling frequency, only the last one is taken
					while (!ServerCommand.END_COMMAND.toString().equals(
							aLineFromClient)) {
						subscriber.setPollFrequency(Integer
								.parseInt(aLineFromClient));
						readALine();
					}
					//for each of service subscribed by this subscriber, polling need to be rescheduled
					for (Service svc:subscriber.getSubscriptionSet()){
						aMonitorService.getServiceMonitors().get(svc.getName()).updateSubscriber(subscriber);
					}
				}
				// set up outage period
				 if (ServerCommand.SET_OUTAGE_PERIOD.toString().equals(
						aLineFromClient)) {
					 isProcessed = true;
					 readALine();
					// format is yyyy-mm-dd hh24:mi:ss<->yyyy-mm-dd hh24:mi:ss
					while (!ServerCommand.END_COMMAND.toString().equals(
							aLineFromClient)) {
						DatePeriod dp = DatePeriod.parseDatePeriod(aLineFromClient);
						subscriber.setOutagePeriod(dp);
						aLineFromClient = input.readLine();
						System.out.println("Receiced From Client:"+subscriber+":"+aLineFromClient);
					}
				}
				
				 if (ServerCommand.QUIT_CLIENT_SESSION.toString().equals(
						aLineFromClient)) {
					 isProcessed = true;
					output.println("bye...");
					output.println(".end.");				

					try {
						for (ServiceMonitor sm : aMonitorService.getServiceMonitors().values()){
							if (sm!=null)
								sm.removeSubscriber(subscriber);
						}
						subscriberSocket.shutdownInput();
						subscriberSocket.shutdownOutput();
						output.close();
						input.close();
						subscriberSocket.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				 if (ServerCommand.LIST_SERVICE.toString().equals(
							aLineFromClient)) {
						 isProcessed = true;
						
					output.println(aMonitorService.getServiceUnderMonitoring());
					output.println(".end.");
				}

				 if (ServerCommand.SHUTDOWN.toString().equals(
						aLineFromClient)) {
					 isProcessed = true;
					output.println("Server is shutting down...");
					output.println(".end.");
					//TODO: more graceful shutting down...
					System.exit(0);
				}
				if (!isProcessed ){
					output.println("Command of "+aLineFromClient+" is not supported");
					output.println(".end.");
					System.out.println("Receiced unsopported command From Client:"+subscriber+":"+aLineFromClient);
					
				}
				isProcessed = false;
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to communicate with client.");
				if (subscriberSocket.isClosed())
					return;
			}
		}
	}

}
