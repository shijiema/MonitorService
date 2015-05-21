package a.ma;

import java.util.List;

public class ServiceCheckingTask implements Runnable {
	private List<ServiceSubscriber> callBacks;
	private Service svc;

	public ServiceCheckingTask(Service svc, List<ServiceSubscriber> callBacks) {
		super();
		this.callBacks = callBacks;
		this.svc = svc;
	}

	@Override
	public void run() {
		System.out.println("Time to check service status. Invoked by "+Thread.currentThread());
		ServiceStatus status = null;
		Service newService = null;
		try {
			 status = ServiceChecker.getServiceStatus(svc);
			 newService = new Service(svc);
			 newService.setStatus(status);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		if(status!=null)
			for(ServiceSubscriber subscriber:callBacks)
				subscriber.update(newService);
	}

}
