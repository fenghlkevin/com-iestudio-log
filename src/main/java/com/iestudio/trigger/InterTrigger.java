package com.iestudio.trigger;

import java.util.Date;

public interface InterTrigger {
	public abstract Date getNextFireTime();
	
	public abstract Date computeFirstFireTime();
	
	public abstract void triggered();
	
	//public abstract Date getStartTime();

	//public abstract Date getEndTime();
}
