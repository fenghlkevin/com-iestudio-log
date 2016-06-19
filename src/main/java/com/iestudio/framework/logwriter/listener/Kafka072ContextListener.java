package com.iestudio.framework.logwriter.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.iestudio.framework.logwriter.hadoop.KafKa072ProducerManager;
import com.kevin.iesutdio.tools.clazz.ObjUtil;

public class Kafka072ContextListener implements ServletContextListener {

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
			System.err.println("初始化kafka072时，无法获取[zookeeper_quorum] 参数");
			return;
		}
		System.out.println("等待kafka072初始化");
		KafKa072ProducerManager km=KafKa072ProducerManager.getInstance();
		km.initializer(zookeeper_quorum);
		if(!km.isInit()){
			System.err.println("初始化kafka072失败");
		}
		
//		try {
//			
//			Thread.sleep(t);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		
	}

	

}
