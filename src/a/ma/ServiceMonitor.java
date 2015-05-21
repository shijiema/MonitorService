package a.ma;
import static java.util.concurrent.TimeUnit.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
/**
 * this solution start a scheduler for each polling frequency as requested.
 * in the real world, polling service status from monitor service is better
 * to be separated from polling request from clients. client should poll the
 * status cached in this service even it's not real time it should be good enough
 * to use. the compromise will result in simpler and more concise code
 * 
 */
public class ServiceMonitor implements Subscribable {
private Service serviceToBeMonitored=null;

private Map<Integer,List<ServiceSubscriber>> pollingMap = Collections.synchronizedMap(new HashMap<Integer,List<ServiceSubscriber>>());
private List<ScheduledFuture<?>> handlers = Collections.synchronizedList(new LinkedList<ScheduledFuture<?>>());
private ScheduledExecutorService scheduler = null;
/**
 * this method guarantees no duplications on subscribers
 */
	@Override
	public void addSubscriber(ServiceSubscriber subscriber) {
			List<ServiceSubscriber> subscribersOfSamePoll = pollingMap.get(new Integer(subscriber.getPollFrequency()));
			if (subscribersOfSamePoll==null){
				subscribersOfSamePoll =  Collections.synchronizedList(new LinkedList<ServiceSubscriber>()); 
				subscribersOfSamePoll.add(subscriber);
				pollingMap.put(new Integer(subscriber.getPollFrequency()), subscribersOfSamePoll);
			}else{
				if (!subscribersOfSamePoll.contains(subscriber))
					subscribersOfSamePoll.add(subscriber);
			}
			subscriber.addSubscription(getServiceUnderMonitored());
			reSchedulePolling();

	}
	private void reSchedulePolling() {
		for(ScheduledFuture<?> handler : handlers){
			handler.cancel(true);
		}
		handlers.clear();
		Set<Integer> pollings = pollingMap.keySet();
		if (scheduler!=null)
			scheduler.shutdownNow();
		scheduler = Executors.newScheduledThreadPool(pollings.size());
		//for each polling frequency, start a corresponding service checking task
		//this might flood the service. so it's better to let monitor service has its own polling frequency
		//and let client just checking its cached service status. so that polling from client does not have to
		//be ended as real checking on the service
		for(Integer poll:pollings){
			ScheduledFuture<?> handler =
				       scheduler.scheduleAtFixedRate(new ServiceCheckingTask(serviceToBeMonitored,pollingMap.get(poll)), poll, poll, SECONDS);
			handlers.add(handler);
		}
	}
	public void removeSubscriber(ServiceSubscriber subscriber) {
		Integer pollingKey = new Integer(subscriber.getPollFrequency());
			List<ServiceSubscriber> subscribersOfSamePoll = pollingMap.get(pollingKey);
			if (subscribersOfSamePoll!=null){
				if(subscribersOfSamePoll.remove(subscriber)){
					if(subscribersOfSamePoll.size()==0)
						pollingMap.remove(pollingKey);
					reSchedulePolling();
				}
			}

	}
	public void updateSubscriber(ServiceSubscriber subscriber) {
		Set<ServiceSubscriber> allSubscribers = new HashSet<ServiceSubscriber>();
		allSubscribers.add(subscriber);
		for (Integer p:pollingMap.keySet()){
			allSubscribers.addAll(pollingMap.get(p));
		}
		pollingMap.clear();
		for(ServiceSubscriber ssb:allSubscribers)
			addSubscriber(ssb);
	}

	public ServiceMonitor(Service serviceToBeMonitored) {
		super();
		this.serviceToBeMonitored = serviceToBeMonitored;
	}
	
	public Service getServiceUnderMonitored(){
		return serviceToBeMonitored;
	}

}
