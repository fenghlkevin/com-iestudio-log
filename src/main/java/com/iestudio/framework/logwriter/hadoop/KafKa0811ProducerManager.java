package com.iestudio.framework.logwriter.hadoop;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

public class KafKa0811ProducerManager {
	
	private  static KafKa0811ProducerManager manager;

	/**
	 * kafka config
	 */
	private Properties props;

	private Producer<Integer, String> producer;

	/**
	 * zookeeper 服务地址(ip:port,ip:port)
	 */
//	private String zookeeper_quorum;
	
	public static KafKa0811ProducerManager getInstance(){
		if(manager==null){
			manager=new KafKa0811ProducerManager();
		}
		return manager;
	}
	
	private boolean init=false;

	public void initializer(String zookeeper_quorum) {
		try {
			if (this.producer == null) {
				props = new Properties();
				props.put("serializer.class", "kafka.serializer.DefaultEncoder");
				props.put("metadata.broker.list", zookeeper_quorum);
				producer = new Producer<Integer, String>(new ProducerConfig(props));
				
			}
			init= true;
		} catch (Exception e) {
			e.printStackTrace();
			init= false;
		}finally{
			producer.close();
		}
	}


	public Producer<Integer, byte[]> getProducer() {
		return new Producer<Integer, byte[]>(new ProducerConfig(props));
//		return producer;
	}

	public boolean isInit() {
		return init;
	}


	public void setInit(boolean init) {
		this.init = init;
	}

}
