//package com.iestudio.framework.logwriter.appender;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.HColumnDescriptor;
//import org.apache.hadoop.hbase.HTableDescriptor;
//import org.apache.hadoop.hbase.client.HBaseAdmin;
//import org.apache.hadoop.hbase.client.HTableInterface;
//import org.apache.hadoop.hbase.client.HTablePool;
//import org.apache.hadoop.hbase.client.Put;
//import org.apache.log4j.AppenderSkeleton;
//import org.apache.log4j.spi.LoggingEvent;
//
//import com.iestudio.date.BaseDate;
//import com.iestudio.framework.logwriter.layout.IHBaseItemLayout;
//import com.iestudio.framework.logwriter.util.LogWriterUtil;
//import com.iestudio.framework.logwriter.util.LogWriterUtil.FileItem;
//import com.iestudio.framework.logwriter.util.Util;
//import com.iestudio.object.ObjUtil;
//import com.iestudio.trigger.CronTrigger;
//import com.iestudio.trigger.InterTrigger;
//
///**
// * 存入hbase所使用的Appender
// * 
// * @author fengheliang
// * 
// */
//@SuppressWarnings("deprecation")
//public class HbaseAppender094 extends AppenderSkeleton implements Runnable {
//
//	/**
//	 * 传入的JSON中，Row Key的属性名
//	 */
//	private static final String ROWKEY = "rowkey";
//	/**
//	 * 提交条数设置
//	 */
//	private int batchSize = 100;
//
//	/**
//	 * 线程间隔
//	 */
//	private int threadPeriod = 1000;
//
//	/**
//	 * hbase 表名配置
//	 */
//	private String htablename = "test";
//
//	/**
//	 * hbase 表名配置
//	 */
//	private String lastTablename = "test";
//	/**
//	 * hbase family配置
//	 */
//	private String hbLogFamily = "bg";
//
//	/**
//	 * 写日志的线程个数
//	 */
//	private int hbPools = 2;
//
//	/**
//	 * 临时存储日志的 序列
//	 */
//	private Queue<LoggingEvent> loggingEvents;
//
//	/**
//	 * 执行task的 线程池
//	 */
//	private ScheduledExecutorService executor;
//
//	/**
//	 * task对象
//	 */
//	private ScheduledFuture<?> task;
//
//	/**
//	 * hbase的 配置信息，内部使用，初始化设置
//	 */
//	private Configuration conf;
//
//	/**
//	 * htable 连接池(0.94 可用，0.96中 可能需要调整)
//	 */
//	private HTablePool hTablePool;
//	
//	HBaseAdmin hBaseAdmin = null;
//
//	/**
//	 * 写入的htable对象
//	 */
//	private HTableInterface htable;
//
//	/**
//	 * zookeeper 服务地址
//	 */
//	private String hbase_zookeeper_quorum;
//
//	/**
//	 * zookeeper 端口
//	 */
//	private String hbase_zookeeper_property_clientPort = "2181";
//	
//	/**
//	 * 
//	 * Type:TimeMillis\Date#format\AtomicInteger
//	 * 
//	 * Example:[GISBILL_]&&[_]&&(Date<yyyy-MM-dd>).log
//	 * 
//	 * Result: GISBILL_EXTATTRVALUE_2009-04-16.log
//	 */
//	protected String htableNameFormat;
//
//	protected long startTime;
//
//	protected List<FileItem> fileItmes = null;
//
//	private InterTrigger trigger;
//
//	/**
//	 * 设置写入间隔时间与batchSize 可以同时使用
//	 */
//	protected String dateSwitch;
//
//	protected long dateLength;
//
//	private void initFileFormat() {
//		if (ObjUtil.isEmpty(fileItmes)) {
//			fileItmes = LogWriterUtil.splitFileFormat(htableNameFormat);
//		}
//	}
//
//	private boolean dataSwitchIsNumber = false;
//
//	/**
//	 * log4j初始设置，启动日志处理计划任务
//	 */
//	@Override
//	public void activateOptions() {
//		try {
//			super.activateOptions();
//			// 创建一个计划任务，并自定义线程名
//			executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(HbaseAppender094.class.getName()));
//			// 日志队列
//			loggingEvents = new ConcurrentLinkedQueue<LoggingEvent>();
//			// 启动计划任务，如果run函数有异常任务将中断！
//			task = executor.scheduleWithFixedDelay(this, threadPeriod, threadPeriod, TimeUnit.MILLISECONDS);
//			// 初始化表明format
//			initFileFormat();
//			// 设定初始化表名称
//			htablename = this.createFileName();
//			this.lastTablename = htablename;
//
//			startTime = System.currentTimeMillis();
//
//			System.out.println("HBase ActivateOptions ok!");
//		} catch (Exception e) {
//			System.err.println("Error during HBase activateOptions: " + e);
//		}
//	}
//
//	private boolean init() {
//		try {
//			if (conf == null) {
//				conf = HBaseConfiguration.create();
//				conf.set("hbase.zookeeper.quorum", hbase_zookeeper_quorum);
//				conf.set("hbase.zookeeper.property.clientPort", hbase_zookeeper_property_clientPort);
//				hTablePool = new HTablePool(conf, hbPools);
//			}
//
//			return true;
//		} catch (Exception e) {
//			this.close();
//			System.err.println("Init Hbase fail !");
//			e.printStackTrace();
//			return false;
//		}
//	}
//
//	@Override
//	public void run() {
//		/**
//		 * 如果初始化失败，则不执行下面逻辑;
//		 */
//		if (!this.init()) {
//			return;
//		}
//		try {
//
//			boolean needRoolOver = false;
//			if (batchSize <= loggingEvents.size()) {
//				needRoolOver = true;
//				//System.out.println("[" + this.getName() + "]连接行数到达最大，写入htable表。(最大值为[" + batchSize + "])");
//			}
//
//			int count = loggingEvents.size();
//			if (dataSwitchIsNumber) {
//
//				if (count > 0 && System.currentTimeMillis() >= getDateLength() + startTime) {
//					//System.out.println("[" + this.getName() + "]时间间隔最大，写入htable表。(时间间隔为[" + dateSwitch + "]分钟)");
//					needRoolOver = true;
//				}
//			} else {
//				if (count > 0 && !ObjUtil.isEmpty(trigger) && System.currentTimeMillis() >= getDateLength() + startTime) {
//					Calendar c = Calendar.getInstance();
//					c.setTimeInMillis(startTime);
//					//System.out.println("[" + this.getName() + "]时间trigger达到最大 ，写入htable表。上次切换时间： [" + BaseDate.parseDate(c.getTime(), 0) + "]， 当前时间： [" + BaseDate.parseDate(new Date(), 0)
//							//+ "]，间隔长度  [" + getDateLength() + "]ms ");
//					needRoolOver = true;
//				}
//			}
//
//			// 日志数据超出批量处理大小
//			if (needRoolOver) {
//				rollOver();
//				if (!ObjUtil.isEmpty(trigger)) {
//					this.setDateLength(this.getSwitchTime(trigger));
//				}
//			}
//		} catch (Exception e) {
//			System.err.println("Error run " + e);
//		}
//	}
//
//	private void rollOver() throws IOException {
//	    
//	    /**
//	     * 初始化hbase表
//	     */
//	    String tempTableName = this.createFileName();
//        if (htable == null || !this.lastTablename.equalsIgnoreCase(tempTableName)) {
//            this.initHbaseTalbe(tempTableName);
//        }
//	    
//		LoggingEvent event;
//		List<Put> logs = new ArrayList<Put>();
//		// 循环处理日志队列
//		while ((event = loggingEvents.poll()) != null) {
//			try {
//				
//				// 创建日志并指定ROW KEY
//				String rowkey=null;
//				if(event.getMessage()!=null && (event.getMessage() instanceof IHBaseItemLayout)){
//					IHBaseItemLayout hblayout=(IHBaseItemLayout)event.getMessage();
//					rowkey=hblayout.getRowKey();
//				}
//				
//				if(ObjUtil.isEmpty(rowkey)){
//					rowkey=event.getThreadName() + System.currentTimeMillis();
//				}
//				
//				Put log = null;
//				
//				//System.out.println(layout.format(event));
//				/**
//				 * 将传入的日志信息转换为MAP对象
//				 */
//				Map<String, String> logInfoMap = Util.jsonString2Map(layout.format(event));
//				
//				if(logInfoMap == null || logInfoMap.size() == 0) {
//					log = new Put(rowkey.getBytes());
//					// 写日志内容，默认单字段为 log
//					log.add(hbLogFamily.getBytes(), "log".getBytes(), layout.format(event).getBytes());
//				} else {
//					/**
//					 * 写日志内容
//					 * 以MAP的KEY为字段名，VALUE为字段值
//					 * 约定Row Key的属性名为 rowkey，在传入构造的JSON的时候需注意
//					 * 如果JSON中没有rowkey字段，则使用原rowkey
//					 */
//					if(logInfoMap.get(ROWKEY) != null) {
//						rowkey = logInfoMap.get(ROWKEY);
//						logInfoMap.remove(ROWKEY);
//					}
//					log = new Put(rowkey.getBytes());
//					//迭代MAP，以KEY为字段名，VALUE为字段值
//					for(Iterator<String> iter = logInfoMap.keySet().iterator();iter.hasNext();) {
//						String columnName = iter.next();
//						String columnValue = String.valueOf(logInfoMap.get(columnName));
//						log.add(hbLogFamily.getBytes(), columnName.getBytes(), columnValue.getBytes());
//					}
//				}
//				
//				logs.add(log);
//			} catch (Exception e) {
//				System.err.println("Error logging put " + e);
//			}
//		}
//		// 批量写入HBASE
//		if (logs.size() > 0) {
//			htable.put(logs);
//		}
//		startTime = System.currentTimeMillis();
//	}
//
//	private void initHbaseTalbe(String tempTableName){
//		//HTableInterface lastHtable = htable;
//		try {
//		    if(hBaseAdmin == null){
//		        hBaseAdmin = new HBaseAdmin(this.conf);
//		    }
//		    
//			if (!hBaseAdmin.tableExists(tempTableName)) {// 如果存在要创建的表
//				System.out.println("htable [" + tempTableName + "] is not exist, create new htable");
//				HTableDescriptor tableDescriptor = new HTableDescriptor(tempTableName);
//				tableDescriptor.addFamily(new HColumnDescriptor(hbLogFamily));
//				hBaseAdmin.createTable(tableDescriptor);
//				this.lastTablename = tempTableName;
//			}
//
//			htable = hTablePool.getTable(tempTableName);
//		} catch (Exception e) {
//		    e.printStackTrace();
//			System.err.println("Init Hbase fail !");
//		} finally {
//			/*if (lastHtable != null) {
//				try {
//					lastHtable.close();
//				} catch (IOException e) {
//					System.err.println("close htable error");
//					e.printStackTrace();
//				}
//			}*/
//
//			if (hBaseAdmin != null) {
//				try {
//					hBaseAdmin.close();
//				} catch (IOException e) {
//					System.err.println("close hbaseAdmin error");
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	// /**
//	// * 初始HBASE
//	// *
//	// * @return
//	// */
//	// private boolean initHbase() {
//	// try {
//	// if (conf == null) {
//	// // 根据classpath下hbase-site.xml创建hbase连接，基于zookeeper
//	// conf = HBaseConfiguration.create();
//	// conf.set("hbase.zookeeper.quorum", hbase_zookeeper_quorum);
//	// // conf.set("hbase.zookeeper.quorum",
//	// // "master104,slave1104,slave2104,slave3104");
//	// conf.set("hbase.zookeeper.property.clientPort",
//	// hbase_zookeeper_property_clientPort);
//	// // htable链接池
//	// hTablePool = new HTablePool(conf, hbPools);
//	// createTable(conf);
//	// htable = hTablePool.getTable(htablename);
//	// System.out.println("Init Hbase OK!");
//	// }
//	// return true;
//	// } catch (Exception e) {
//	// task.cancel(false);
//	// executor.shutdown();
//	// System.err.println("Init Hbase fail !");
//	// return false;
//	// }
//	// }
//
//	// private void createTable(Configuration configuration) {
//	//
//	// }
//
//	/**
//	 * 日志事件
//	 * 
//	 * @param loggingEvent
//	 */
//	@Override
//	protected void append(LoggingEvent loggingEvent) {
//		try {
//			// 添加到日志队列
//			loggingEvents.add(loggingEvent);
//		} catch (Exception e) {
//			System.err.println("Error populating event and adding to queue" + e);
//		}
//	}
//
//	@Override
//	public void close() {
//		try {
//			if (task != null) {
//				task.cancel(false);
//			}
//			if (executor != null) {
//				executor.shutdown();
//			}
//			if (htable != null) {
//				htable.close();
//			}
//			if (hTablePool != null) {
//				hTablePool.close();
//			}
//			// htable的关闭，需要在切换表的过程中进行
//			// htable.close();
//		} catch (IOException e) {
//			System.err.println("Error close " + e);
//		}
//	}
//
//	@Override
//	public boolean requiresLayout() {
//		return true;
//	}
//
//	// // 设置每一批日志处理数量
//	// public void setBatchSize(int batchSize) {
//	// this.batchSize = batchSize;
//	// }
//	//
//	// /**
//	// * 设置计划任务执行间隔
//	// *
//	// * @param period
//	// */
//	// public void setPeriod(int period) {
//	// this.period = period;
//	// }
//
//	public String getHtablename() {
//		return htablename;
//	}
//
//	public int getBatchSize() {
//		return batchSize;
//	}
//
//	public void setBatchSize(int batchSize) {
//		this.batchSize = batchSize;
//	}
//
//	public int getThreadPeriod() {
//		return threadPeriod;
//	}
//
//	public void setThreadPeriod(int threadPeriod) {
//		this.threadPeriod = threadPeriod;
//	}
//
//	public int getHbPools() {
//		return hbPools;
//	}
//
//	public void setHbPools(int hbPools) {
//		this.hbPools = hbPools;
//	}
//
//	public void setHtablename(String htablename) {
//		this.htablename = htablename;
//	}
//
//	public String getHbase_zookeeper_quorum() {
//		return hbase_zookeeper_quorum;
//	}
//
//	public void setHbase_zookeeper_quorum(String hbase_zookeeper_quorum) {
//		this.hbase_zookeeper_quorum = hbase_zookeeper_quorum;
//	}
//
//	public String getHbase_zookeeper_property_clientPort() {
//		return hbase_zookeeper_property_clientPort;
//	}
//
//	public void setHbase_zookeeper_property_clientPort(String hbase_zookeeper_property_clientPort) {
//		this.hbase_zookeeper_property_clientPort = hbase_zookeeper_property_clientPort;
//	}
//
//	/**
//	 * 日志表的列族名字
//	 * 
//	 * @param hbLogFamily
//	 */
//	public void setHbLogFamily(String hbLogFamily) {
//		this.hbLogFamily = hbLogFamily;
//	}
//
//	public String getHtableNameFormat() {
//		return htableNameFormat;
//	}
//
//	public void setHtableNameFormat(String fileFormat) {
//		if (ObjUtil.isEmpty(fileFormat)) {
//			fileFormat = "[DEFAULT_LOG_]%%(TimeMillis)%%[_]%%(AtomicInteger)";
//		}
//		this.htableNameFormat = fileFormat;
//	}
//
//	public String getDateSwitch() {
//		return dateSwitch;
//	}
//
//	public void setDateSwitch(String dateSwitch) {
//		if ("-1".equalsIgnoreCase(dateSwitch)) {
//			return;
//		}
//		if (ObjUtil.isEmpty(dateSwitch)) {
//			this.dateSwitch = "15";
//		} else {
//			this.dateSwitch = dateSwitch;
//		}
//		if (ObjUtil.isNumber(this.getDateSwitch()) && new Integer(this.getDateSwitch()).intValue() > 0) {
//			this.setDateLength(new Integer(this.getDateSwitch()) * 60 * 1000);
//		} else {
//			try {
//				trigger = new CronTrigger(new Date(), this.getDateSwitch());
//			} catch (ParseException e) {
//				e.printStackTrace();
//				this.dateSwitch = "15";
//				this.setDateLength(15 * 60 * 1000);
//				trigger = null;
//			}
//			this.setDateLength(getSwitchTime(trigger));
//		}
//
//		if (ObjUtil.isNumber(this.dateSwitch)) {
//			dataSwitchIsNumber = true;
//		} else {
//			dataSwitchIsNumber = false;
//		}
//	}
//
//	public long getDateLength() {
//		return dateLength;
//	}
//
//	public void setDateLength(long dateLength) {
//		this.dateLength = dateLength;
//	}
//
//	private long getSwitchTime(InterTrigger trigger) {
//		Date nextFireTime = trigger.getNextFireTime();
//		if (nextFireTime == null) {
//			nextFireTime = trigger.computeFirstFireTime();
//		} else {
//			trigger.triggered();
//			nextFireTime = trigger.getNextFireTime();
//		}
//
//		Calendar endTime = Calendar.getInstance();
//		endTime.setTime(nextFireTime);
//		Calendar startTime = Calendar.getInstance();
//		startTime.setTime(new Date());
//		long temp = (endTime.getTimeInMillis() - startTime.getTimeInMillis());
//		//System.out.println("时间间隔为 [" + temp + "], endTime : [" + BaseDate.parseDate(endTime.getTime(), 0) + "],startTime : [" + BaseDate.parseDate(startTime.getTime(), 0) + "]");
//		while (temp < -0) {
//			temp = getSwitchTime(trigger);
//		}
//		// System.out.println("nextFireTime : [" + new
//		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextFireTime) + "]");
//		return temp;
//	}
//
//	private String createFileName() {
//		StringBuffer str = new StringBuffer();
//		if (fileItmes != null) {
//			for (int i = 0; i < fileItmes.size(); i++) {
//				str.append(fileItmes.get(i).getValue(this));
//			}
//			return str.toString();
//		} else {
//			return "DEFAULT_LOG";
//		}
//	}
//
//	public static class NamedThreadFactory implements ThreadFactory {
//		private final String prefix;
//		private final ThreadFactory threadFactory;
//		private final AtomicInteger atomicInteger = new AtomicInteger();
//
//		public NamedThreadFactory(final String prefix) {
//			this(prefix, Executors.defaultThreadFactory());
//		}
//
//		public NamedThreadFactory(final String prefix, final ThreadFactory threadFactory) {
//			this.prefix = prefix;
//			this.threadFactory = threadFactory;
//		}
//
//		@Override
//		public Thread newThread(Runnable r) {
//			Thread t = this.threadFactory.newThread(r);
//			t.setName(this.prefix + this.atomicInteger.incrementAndGet());
//			return t;
//		}
//	}
//}