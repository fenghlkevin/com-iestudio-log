package com.iestudio.framework.logwriter.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.iestudio.framework.logwriter.hadoop.KafKa0811ProducerManager;
import com.iestudio.object.ObjUtil;

public class Kafka0811ContextListener implements ServletContextListener {

	public static String SYSTEM_KAFKA_KEY="kafka.listener.time";
	
	public static String SYSTEM_ZOOKEEPER_QUORUM="zookeeper_quorum";
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String time=System.getProperty("SYSTEM_KAFKA_KEY");
		if(time==null||"".equalsIgnoreCase(time)||!ObjUtil.isNumber(time)){
			time="60000";
		}
		
		Long t=Long.valueOf(time);
		
		String zookeeper_quorum=System.getProperty("zookeeper_quorum");
		if(zookeeper_quorum==null||"".equalsIgnoreCase(zookeeper_quorum)){
			System.err.println("初始化kafka0811时，无法获取[zookeeper_quorum] 参数");
			return;
		}
		System.out.println("等待kafka0811初始化");
		KafKa0811ProducerManager km=KafKa0811ProducerManager.getInstance();
		km.initializer(zookeeper_quorum);
		if(!km.isInit()){
			System.err.println("初始化kafka0811失败");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {		
	}

}
