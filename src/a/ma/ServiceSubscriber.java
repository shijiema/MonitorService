/**
 * 
 */
package a.ma;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * this class represents a monitor service subscriber for convenience purpose, a
 * client socket is used as a mean to communicate with subscriber
 *
 */
public class ServiceSubscriber implements Updatable {
	// the subscriber. a cient socket
	Socket subscriberSocket;
	DatePeriod outagePeriod;
	Set<Service> subscriptionSet = Collections
			.synchronizedSet(new HashSet<Service>());
	// number of seconds before sending back notification. default 5 seconds
	int gracePeriod = 3;
	// number of seconds of poll frequency. minimum 1 second per polling.
	// default 10 seconds
	int pollFrequency = 10;
	// use this to make sure the notification happens not more frequent than the
	// poll frequency
	Map<Service,ServiceNote> serviceLastStatus = Collections.synchronizedMap(new HashMap<Service,ServiceNote>());

	public Socket getSubscriber() {
		return subscriberSocket;
	}

	/**
	 * after created, it does not change subscriber
	 * 
	 * @param subscriber
	 */
	public ServiceSubscriber(Socket subscriber) {
		this.subscriberSocket = subscriber;
	}

	public Set<Service> getSubscriptionSet() {
		return subscriptionSet;
	}

	public void addSubscription(Service svc) {
		this.subscriptionSet.add(svc);
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public int getPollFrequency() {
		return pollFrequency;
	}

	public void setPollFrequency(int pollFrequency) {
		if (pollFrequency < 1)
			pollFrequency = 1;
		this.pollFrequency = pollFrequency;
	}

	public DatePeriod getOutagePeriod() {
		return outagePeriod;
	}

	public void setOutagePeriod(DatePeriod outagePeriod) {
		this.outagePeriod = outagePeriod;
	}
	public boolean isScheduledOutage(){
		if (outagePeriod==null)return false;
		return outagePeriod.isBetween(Calendar.getInstance().getTime());
	}

	@Override
	public void update(Service svc) {
		ServiceStatus status = svc.getStatus();
		Date rightNow = Calendar.getInstance().getTime();
		//do not update if the client is in planned outage
		if(outagePeriod!=null && outagePeriod.isBetween(rightNow))return;
		//do not update if the service is down and last update is also down and grace period is still not past
		//
		ServiceNote sn = serviceLastStatus.get(svc);
		if(ServiceStatus.DOWN.equals(status)&&
				sn!=null && 
						sn.getSvc()!=null &&
								ServiceStatus.DOWN.equals(sn.getSvc().getStatus()) &&
									sn.getUpdateTime()!=null && 
				(rightNow.getTime() - sn.getUpdateTime().getTime())<(this.pollFrequency+this.gracePeriod)*1000)
			return;
		
		
		//TOASK: if service status is not changed, then do not update
		//for easier debugging and observation, leaving it to update back according to polling frequency for now
		if (subscriberSocket!=null && !subscriberSocket.isClosed()){
			PrintWriter output = null;
			try {
				output = new PrintWriter(subscriberSocket.getOutputStream(), true);
				output.println(DateUtil.formatDate(Calendar.getInstance().getTime())+":"+svc.getName()+":"+status);
				ServiceNote snNew = new ServiceNote(svc,rightNow);
				serviceLastStatus.put(svc, snNew);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to update client about service status change");
			}
		}

	}

	@Override
	public boolean equals(Object obj) {

		return (obj instanceof ServiceSubscriber && subscriberSocket
				.equals(((ServiceSubscriber) obj).getSubscriber()));
	}

	@Override
	public int hashCode() {

		return subscriberSocket.hashCode();
	}

	@Override
	public String toString() {

		return subscriberSocket.toString();
	}

}
