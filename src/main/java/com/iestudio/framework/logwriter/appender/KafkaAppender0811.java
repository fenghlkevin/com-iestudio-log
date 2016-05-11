package com.iestudio.framework.logwriter.appender;

import java.util.List;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.spi.LoggingEvent;

import cn.com.cennavi.kfgis.util.ObjectUtil;

import com.iestudio.framework.logwriter.appender.absappender.AbstractRunnableAppender;
import com.iestudio.framework.logwriter.hadoop.KafKa0811ProducerManager;
import com.iestudio.framework.logwriter.util.ZipUtil;

/**
 * 存入kafka所使用的Appender
 * 
 * @author fengheliang
 * 
 */
public class KafkaAppender0811 extends AbstractRunnableAppender<KeyedMessage<Integer, byte[]>> implements Runnable {
	/**
	 * kafka config
	 */
	// private Properties props;

	// private Producer<Integer, String> producer;

	/**
	 * zookeeper 服务地址(ip:port,ip:port)
	 */
	private String zookeeper_quorum;

	/**
	 * hbase 表名配置
	 */
	private String topic = "test";

	@Override
	protected boolean initializer() {
		KafKa0811ProducerManager km = KafKa0811ProducerManager.getInstance();
		// this.producer = km.getProducer();
		return km.isInit();
	}
	@Override
	protected RollOverLogs<KeyedMessage<Integer, byte[]>> createRollOver() {
		final String topic = this.topic;
		try {

			return new RollOverLogs<KeyedMessage<Integer, byte[]>>() {

				@Override
				protected void addLog(List<KeyedMessage<Integer, byte[]>> logs, LoggingEvent event) {
					
					//byte[] bs = ObjectUtil.getObjectBytes(event.getMessage());					
					 byte[] bs = ObjectUtil.getObjectBytesByTopic(event.getMessage());
					 System.out.println("topic-----"+topic+";temp-------"+new String(bs));
					 logs.add(new KeyedMessage<Integer, byte[]>(topic, ZipUtil.gZip(bs)));
				}

				@Override
				protected void pushLog(List<KeyedMessage<Integer, byte[]>> logs) {
					Producer<Integer, byte[]> producer;
					boolean myborrow=true;
					try {
						producer = pool.borrowObject();
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Can not borrow producer,new one");
						myborrow=false;
						producer=KafKa0811ProducerManager.getInstance().getProducer();
					}
					try {
						if (producer != null) {
							producer.send(logs);
						}
					}catch (Exception e) {
						
						try {
							if (producer != null) {
								producer.send(logs);
							}
						}catch (Exception e1) {
							if (producer != null) {
								producer.send(logs);
							}
						}
					}
					finally {
						if(myborrow){
							pool.returnObject(producer);
						}else if(producer!=null){
							producer.close();
						}
					}
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void closeStream() {

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

	@Override
	public void activateOptions() {
		super.activateOptions();
		if (pool == null) {
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxIdle(this.getHbPools()*2);
			config.setMaxTotal(this.getHbPools()*3);
			config.setBlockWhenExhausted(true);
			config.setMaxWaitMillis(500);
			config.setMinIdle(this.getHbPools()+1);
			pool = new GenericObjectPool<Producer<Integer, byte[]>>( new KafkaProducerPooFactory(), config);
		}
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	private GenericObjectPool<Producer<Integer, byte[]>> pool;

	private class KafkaProducerPooFactory extends BasePooledObjectFactory<Producer<Integer, byte[]>> {
		

		@Override
		public Producer<Integer, byte[]> create() throws Exception {
			Producer<Integer, byte[]> producer= KafKa0811ProducerManager.getInstance().getProducer();
			return producer;
		}

		@Override
		public void destroyObject(PooledObject<Producer<Integer, byte[]>> p) throws Exception {
			p.getObject().close();
		}

		@Override
		public PooledObject<Producer<Integer, byte[]>> wrap(Producer<Integer, byte[]> obj) {
			return new DefaultPooledObject<Producer<Integer, byte[]>>(obj);
			// return null;
		}

	}

}