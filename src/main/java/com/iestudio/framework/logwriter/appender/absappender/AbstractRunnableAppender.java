package com.iestudio.framework.logwriter.appender.absappender;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.iestudio.object.ObjUtil;
import com.iestudio.trigger.CronTrigger;
import com.iestudio.trigger.InterTrigger;

public abstract class AbstractRunnableAppender<T> extends AppenderSkeleton implements Runnable {

	/**
	 * 临时存储日志的 序列
	 */
	private Queue<LoggingEvent> loggingEvents;

	/**
	 * 执行task的 线程池
	 */
	private ScheduledExecutorService executor;

	/**
	 * task对象
	 */
	private ScheduledFuture<?> task;

	protected long startTime;

	private InterTrigger trigger;

	protected long dateLength;

	private boolean dataSwitchIsNumber = false;

	/**
	 * 设置写入间隔时间与batchSize 可以同时使用
	 */
	protected String dateSwitch;

	/**
	 * 提交条数设置
	 */
	private int batchSize = 100;

	/**
	 * 线程间隔
	 */
	private int threadPeriod = 1000;

	/**
	 * 写日志的线程个数
	 */
	private int hbPools = 2;
	
	/**
	 * log4j初始设置，启动日志处理计划任务
	 */
	@Override
	public void activateOptions() {
		try {
			super.activateOptions();
			// 创建一个计划任务，并自定义线程名
//			executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(this.getName()));
			executor = Executors.newScheduledThreadPool(hbPools+1,new NamedThreadFactory(this.getName()));
			// 日志队列
			loggingEvents = new ConcurrentLinkedQueue<LoggingEvent>();
			// 启动计划任务，如果run函数有异常任务将中断！
			task = executor.scheduleWithFixedDelay(this, threadPeriod, threadPeriod, TimeUnit.MILLISECONDS);
			// 初始化kafka

			startTime = System.currentTimeMillis();

		} catch (Exception e) {
			System.err.println("Error during Thread Log activateOptions: " + e);
		}
	}

	protected abstract boolean initializer();
	
	protected boolean initFlag=false;
	@Override
	public void run() {
		/**
		 * 如果初始化失败，则不执行下面逻辑;
		 */
		if (!this.initializer()) {
			return;
		}
		initFlag=true;
		try {

			boolean needRoolOver = false;
//			if (batchSize <= loggingEvents.size()) {
//				needRoolOver = true;
//			}
//
//			int count = loggingEvents.size();
			if (dataSwitchIsNumber) {

				if (System.currentTimeMillis() >= getDateLength() + startTime) {//count > 0 && 
					needRoolOver = true;
				}
			} else {
				if (!ObjUtil.isEmpty(trigger) && System.currentTimeMillis() >= getDateLength() + startTime) {//count > 0 && 
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(startTime);
					needRoolOver = true;
				}
			}

			// 日志数据超出批量处理大小
			if (needRoolOver) {
				rollOver();
				if (!ObjUtil.isEmpty(trigger)) {
					this.setDateLength(this.getSwitchTime(trigger));
				}
			}
		} catch (Exception e) {
			System.err.println("Error run " + e);
		}
	}

	protected abstract class RollOverLogs<D> implements Runnable{
		
		@Override
		public void run() {
			rollover(loggingEvents,batchSize);
		}

		protected void rollover(Queue<LoggingEvent> loggingEvents, int batchSize) {
			LoggingEvent event;
			List<D> logs = new ArrayList<D>();
			int count = 0;
			while (count < (batchSize + 100) && (event = loggingEvents.poll()) != null) {
				this.addLog(logs, event);
				count++;
			}
			// 批量写入
			if (logs.size() > 0) {
				this.pushLog(logs);
			}
		}

		protected abstract void addLog(List<D> logs, LoggingEvent event);

		protected abstract void pushLog(List<D> logs);
	}

	protected abstract RollOverLogs<T> createRollOver();

	private void rollOver() throws IOException {
		for(int i=0;i<this.hbPools;i++){
			RollOverLogs<T> ro = this.createRollOver();
			if(ro!=null){
				this.executor.execute(ro);
			}
			
		}
		
//		ro.rollover(loggingEvents, batchSize);

		startTime = System.currentTimeMillis();
	}
	
	/**
	 * 日志事件
	 * 
	 * @param loggingEvent
	 */
	@Override
	protected void append(LoggingEvent loggingEvent) {
		if(!initFlag){
			System.err.println("appender ["+this.getClass().getName()+"] init error. Can not add log to quene!");
			return;
		}
		try {
			// 添加到日志队列
			loggingEvents.add(loggingEvent);
		} catch (Exception e) {
			System.err.println("Error populating event and adding to queue" + e);
		}
	}
	
	protected abstract void closeStream();

	@Override
	public void close() {
		if (task != null) {
			task.cancel(false);
		}
		if (executor != null) {
			executor.shutdown();
		}
		closeStream();
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getThreadPeriod() {
		return threadPeriod;
	}

	public void setThreadPeriod(int threadPeriod) {
		this.threadPeriod = threadPeriod;
	}

	public int getHbPools() {
		return hbPools;
	}

	public void setHbPools(int hbPools) {
		this.hbPools = hbPools;
	}


	public String getDateSwitch() {
		return dateSwitch;
	}

	public void setDateSwitch(String dateSwitch) {
		if ("-1".equalsIgnoreCase(dateSwitch)) {
			return;
		}
		if (ObjUtil.isEmpty(dateSwitch)) {
			this.dateSwitch = "15";
		} else {
			this.dateSwitch = dateSwitch;
		}
		if (ObjUtil.isNumber(this.getDateSwitch()) && new Integer(this.getDateSwitch()).intValue() > 0) {
			this.setDateLength(new Integer(this.getDateSwitch()) *60* 1000);
		} else {
			try {
				trigger = new CronTrigger(new Date(), this.getDateSwitch());
			} catch (ParseException e) {
				e.printStackTrace();
				this.dateSwitch = "15";
				this.setDateLength(15 * 60 * 1000);
				trigger = null;
			}
			this.setDateLength(getSwitchTime(trigger));
		}

		if (ObjUtil.isNumber(this.dateSwitch)) {
			dataSwitchIsNumber = true;
		} else {
			dataSwitchIsNumber = false;
		}
	}

	public long getDateLength() {
		return dateLength;
	}

	public void setDateLength(long dateLength) {
		this.dateLength = dateLength;
	}

	private long getSwitchTime(InterTrigger trigger) {
		Date nextFireTime = trigger.getNextFireTime();
		if (nextFireTime == null) {
			nextFireTime = trigger.computeFirstFireTime();
		} else {
			trigger.triggered();
			nextFireTime = trigger.getNextFireTime();
		}

		Calendar endTime = Calendar.getInstance();
		endTime.setTime(nextFireTime);
		Calendar startTime = Calendar.getInstance();
		startTime.setTime(new Date());
		long temp = (endTime.getTimeInMillis() - startTime.getTimeInMillis());
		while (temp < -0) {
			temp = getSwitchTime(trigger);
		}
		return temp;
	}

	public static class NamedThreadFactory implements ThreadFactory {
		private final String prefix;
		private final ThreadFactory threadFactory;
		private final AtomicInteger atomicInteger = new AtomicInteger();

		public NamedThreadFactory(final String prefix) {
			this(prefix, Executors.defaultThreadFactory());
		}

		public NamedThreadFactory(final String prefix, final ThreadFactory threadFactory) {
			this.prefix = prefix;
			this.threadFactory = threadFactory;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = this.threadFactory.newThread(r);
			t.setName(this.prefix + this.atomicInteger.incrementAndGet());
			return t;
		}
	}

}
