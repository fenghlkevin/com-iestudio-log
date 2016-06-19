//package com.iestudio.framework.logwriter.util;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//import kafka.producer.KeyedMessage;
//import kafka.producer.ProducerConfig;
//
//public class Producer extends Thread {
//	public static  kafka.javaapi.producer.Producer<Integer, String> producer;
//	private final String topic;
//	private final Properties props = new Properties();
//
//	public Producer(String topic) {
//		props.put("serializer.class", "kafka.serializer.StringEncoder");
//		props.put("metadata.broker.list", "192.168.59.163:9092");
//		producer = new kafka.javaapi.producer.Producer<Integer, String>(new ProducerConfig(props));
//		this.topic = topic;
//	}
//	public static void main(String[] args) {
//		Producer p = new Producer("LOGBMWREQLOG");
//		List<KeyedMessage<Integer, String>> messageList = new ArrayList<KeyedMessage<Integer, String>>();
//		for(int i = 0;i < 10;i ++) {
//			String messageStr = new String("Message_" + i);
//			KeyedMessage<Integer, String> message = new KeyedMessage<Integer, String>("LOGBMWREQLOG", messageStr);
//			messageList.add(message);
//		}
//		producer.send(messageList);
//		System.out.println("send ok");
//	}
//	public void run() {
//
//		List<KeyedMessage<Integer, String>> messageList = new ArrayList<KeyedMessage<Integer, String>>();
//		for(int i = 0;i < 100;i ++) {
//			String messageStr = new String("Message_" + i);
//			KeyedMessage<Integer, String> message = new KeyedMessage<Integer, String>(topic, messageStr);
//			messageList.add(message);
//		}
//		producer.send(messageList);
//	}
//
//}
