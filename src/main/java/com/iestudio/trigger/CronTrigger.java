package com.iestudio.trigger;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("unchecked")
public class CronTrigger implements InterTrigger{

	private CronExpression cronEx = null;
	private Date startTime = null;
	private Date endTime = null;

	private Date nextFireTime = null;
	@SuppressWarnings("unused")
	private Date previousFireTime = null;
	private transient TimeZone timeZone = null;

	public CronTrigger(Date date,String cronExpression) throws ParseException {
		setStartTime(date);
		setTimeZone(TimeZone.getDefault());
		setCronExpression(cronExpression);
	}

	public void setCronExpression(String cronExpression) throws ParseException {
		TimeZone origTz = getTimeZone();
		this.cronEx = new CronExpression(cronExpression);
		this.cronEx.setTimeZone(origTz);
	}

	public TimeZone getTimeZone() {

		if (cronEx != null) {
			return cronEx.getTimeZone();
		}

		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		return timeZone;
	}

	public void setStartTime(Date startTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("Start time cannot be null");
		}

		Date eTime = getEndTime();
		if (eTime != null && startTime != null && eTime.before(startTime)) {
			throw new IllegalArgumentException(
					"End time cannot be before start time");
		}

		// round off millisecond...
		// Note timeZone is not needed here as parameter for
		// Calendar.getInstance(),
		// since time zone is implicit when using a Date in the setTime
		// method.
		Calendar cl = Calendar.getInstance();
		cl.setTime(startTime);
		cl.set(Calendar.MILLISECOND, 0);

		this.startTime = cl.getTime();
	}

	public void setTimeZone(TimeZone timeZone) {
		if (cronEx != null) {
			cronEx.setTimeZone(timeZone);
		}
		this.timeZone = timeZone;
	}

	public Date getFireTimeAfter(Date afterTime) {
		if (afterTime == null) {
			afterTime = new Date();
		}

		if (getStartTime().after(afterTime)) {
			afterTime = new Date(getStartTime().getTime() - 1000l);
		}

		if (getEndTime() != null && (afterTime.compareTo(getEndTime()) >= 0)) {
			return null;
		}

		Date pot = getTimeAfter(afterTime);
		if (getEndTime() != null && pot != null && pot.after(getEndTime())) {
			return null;
		}

		return pot;
	}

	public Date computeFirstFireTime() {
		nextFireTime = getFireTimeAfter(new Date(
				getStartTime().getTime() - 1000l));

		// while (nextFireTime != null && calendar != null &&
		// !calendar.isTimeIncluded(nextFireTime.getTime())) {
		// nextFireTime = getFireTimeAfter(nextFireTime);
		// }

		return nextFireTime;
	}

//	public static List computeFireTimes(MyTrigger t, org.quartz.Calendar cal,
//			int numTimes) {
//		LinkedList lst = new LinkedList();
//
//		// CronTrigger t = (CronTrigger) trigg.clone();
//
//		if (t.getNextFireTime() == null) {
//			t.computeFirstFireTime();
//		}
//
//		for (int i = 0; i < numTimes; i++) {
//			Date d = t.getNextFireTime();
//			if (d != null) {
//				lst.add(d);
//				t.triggered(cal);
//			} else {
//				break;
//			}
//		}
//
//		return java.util.Collections.unmodifiableList(lst);
//	}

	public void triggered() {
		previousFireTime = nextFireTime;
		nextFireTime = getFireTimeAfter(nextFireTime);

//		while (nextFireTime != null && calendar != null
//				&& !calendar.isTimeIncluded(nextFireTime.getTime())) {
//			nextFireTime = getFireTimeAfter(nextFireTime);
//		}
	}

	// public static void main(String[] args)
	// // unit testing
	// throws Exception {
	//
	// // String expr = "15 10 0/4 * * ?";
	//
	// String expr = "0 0 0/24 * * ?";
	// if (args != null && args.length > 0 && args[0] != null) {
	// expr = args[0];
	// }
	//
	// //CronTrigger ct = new CronTrigger("t", "g", "j", "g", new Date(), null,
	// expr);
	// MyTrigger ct=new MyTrigger(expr);
	// // ct.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	// //System.err.println(ct.getExpressionSummary());
	// System.err.println("tz=" + ct.getTimeZone().getID());
	// System.err.println();
	//
	// java.util.List times = computeFireTimes(ct, null, 25);
	//
	// for (int i = 0; i < times.size(); i++) {
	// System.err.println("firetime = " + times.get(i));
	// }
	//
	// Calendar tt = Calendar.getInstance();
	// tt.set(Calendar.DATE, 17);
	// tt.set(Calendar.MONTH, 5 - 1);
	// tt.set(Calendar.HOUR, 11);
	// tt.set(Calendar.MINUTE, 0);
	// tt.set(Calendar.SECOND, 7);
	//
	// //System.err.println("\nWill fire on: " + tt.getTime() + " -- " +
	// ct.willFireOn(tt, false));
	//
	// }

	public Date getStartTime() {
		return this.startTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	protected Date getTimeAfter(Date afterTime) {
		return (cronEx == null) ? null : cronEx.getTimeAfter(afterTime);
	}

	public Date getNextFireTime() {
		return this.nextFireTime;
	}

}
