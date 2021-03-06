package a.ma;

public class Service {
	private String name;
	private String IP;
	private int port;
	private ServiceStatus status;
	/**
	 * This class represents a TCP/IP service. it's possible to check validity
	 * of IP address and tcp port in this class
	 * 
	 * @param iP
	 * @param port
	 */

	public Service(String name, String iP, int port) {

		IP = iP;
		this.port = port;
		this.name = name;
	}
	public Service(Service svc) {

		this.IP = svc.getIP();
		this.port = svc.getPort();
		this.name = svc.getName();
		this.status = svc.getStatus();
	}
	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPort(int port) throws Exception {
		if (port < 0 || port > 65535)
			throw new Exception("Invaid port number.");
		this.port = port;
	}

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
		this.status = status;
	}

	public String toString(){
		return name+"="+IP+":"+String.valueOf(port);
	}
	//automatically generated by eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((IP == null) ? 0 : IP.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + port;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Service other = (Service) obj;
		if (IP == null) {
			if (other.IP != null)
				return false;
		} else if (!IP.equals(other.IP))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port != other.port)
			return false;
		if (status != other.status)
			return false;
		return true;
	}
	
}
