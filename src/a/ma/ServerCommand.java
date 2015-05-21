package a.ma;

public enum ServerCommand {
	END_COMMAND(".end."),
	LIST_SERVICE("list service:"),
	REGISTER_SERVICE("register service:"),
	SUBSCRIBE_SERVICE("subscribe:"),
	SET_GRACE_PERIOD("set grace period:"),
	SET_POLLING_PERIOD("set polling:"),
	SET_OUTAGE_PERIOD("set outage period:"),
	QUIT_CLIENT_SESSION("quit:"),
	SHUTDOWN("shutdown:");
	
private final String cmdStr;
public String toString(){
	return cmdStr;
}

ServerCommand(String desc){
	cmdStr = desc;
}
}
