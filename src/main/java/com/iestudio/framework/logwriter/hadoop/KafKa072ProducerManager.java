package com.iestudio.framework.logwriter.hadoop;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

public class KafKa072ProducerManager {
	
	private  static KafKa072ProducerManager manager;

	/**
	 * kafka config
	 */
	private Properties props;

	private Producer<Integer, String> producer;

	/**
	 * zookeeper 服务地址(ip:port,ip:port)
	 */
//	private String zookeeper_quorum;
	
	public static KafKa072ProducerManager getInstance(){
		if(manager==null){
			manager=new KafKa072ProducerManager();
		}
		return manager;
	}
	
	private boolean init=false;

	public void initializer(String zookeeper_quorum) {
		try {
			if (this.producer == null) {
				props = new Properties();
				
				props.put("serializer.class", "kafka.serializer.StringEncoder");
				props.put("zk.connect", zookeeper_quorum);
				
				producer = new Producer<Integer, String>(new ProducerConfig(props));
			}
			init= true;
		} catch (Exception e) {
			e.printStackTrace();
			//System.err.println("");
			init= false;
		}
	}


	public Producer<Integer, String> getProducer() {
		return producer;
	}

	public boolean isInit() {
		return init;
	}


	public void setInit(boolean init) {
		this.init = init;
	}

}
