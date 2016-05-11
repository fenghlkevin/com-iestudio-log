/*package com.iestudio.framework.logwriter.appender;

import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
//import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

import org.apache.log4j.spi.LoggingEvent;

import cn.com.cennavi.kfgis.util.ObjectUtil;
import cn.com.cennavi.kfgis.util.SBase64;

import com.iestudio.framework.logwriter.appender.absappender.AbstractRunnableAppender;
import com.iestudio.framework.logwriter.hadoop.KafKa072ProducerManager;

*//**
 * 存入kafka所使用的Appender
 * 
 * @author fengheliang
 * 
 *//*
public class KafkaAppender072 extends AbstractRunnableAppender<ProducerData<Integer, String>> implements Runnable {

	*//**
	 * kafka config
	 *//*

	private Producer<Integer, String> producer;

	*//**
	 * zookeeper 服务地址(ip:port,ip:port)
	 *//*
	private String zookeeper_quorum;

	*//**
	 * hbase 表名配置
	 *//*
	private String topic = "test";

	@Override
	protected boolean initializer() {
		KafKa072ProducerManager km = KafKa072ProducerManager.getInstance();
		this.producer = km.getProducer();
		return km.isInit();
	}

	@Override
	protected RollOver<ProducerData<Integer, String>> createRollOver() {
		final String topic = this.topic;
		final Producer<Integer, String> producer = this.producer;
		return new RollOver<ProducerData<Integer, String>>() {

			@Override
			protected void addLog(List<ProducerData<Integer, String>> logs, LoggingEvent event) {
				byte[] bs = ObjectUtil.getObjectBytes(event.getMessage());
				logs.add(new ProducerData<Integer, String>(topic, SBase64.encode(bs)));
			}

			@Override
			protected void pushLog(List<ProducerData<Integer, String>> logs) {
				producer.send(logs);
			}
		};
	}

	@Override
	protected void closeStream() {
		if (this.producer != null) {
			this.producer.close();
		}

	}

	public String getZookeeper_quorum() {
		return zookeeper_quorum;
	}

	public void setZookeeper_quorum(String zookeeper_quorum) {
		this.zookeeper_quorum = zookeeper_quorum;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	

}*/