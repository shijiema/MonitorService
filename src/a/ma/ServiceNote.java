package a.ma;

import java.util.Date;

public class ServiceNote {
private Service svc;
private Date updateTime;
public Service getSvc() {
	return svc;
}
public Date getUpdateTime() {
	return updateTime;
}
public ServiceNote(Service svc, Date updateTime) {
	super();
	this.svc = svc;
	this.updateTime = updateTime;
}

}
