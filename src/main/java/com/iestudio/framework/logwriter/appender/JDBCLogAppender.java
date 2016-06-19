package com.iestudio.framework.logwriter.appender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
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

import com.kevin.iesutdio.tools.clazz.ObjUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.iestudio.framework.logwriter.util.LogWriterUtil;
import com.iestudio.framework.logwriter.util.LogWriterUtil.FileItem;
import com.iestudio.trigger.CronTrigger;
import com.iestudio.trigger.InterTrigger;

/**
 * Created on 2014-5-19
 * <p>Title: 实现存储日志到mysql类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: 沈阳世纪高通科技有限公司</p>
 * <p>Department: 技术开发部</p>
 * 
 * @author caodezhi@cennavi.com.cn
 * @version 1.0
 * @update 修改日期 修改描述
 */
public class JDBCLogAppender extends AppenderSkeleton implements Runnable {

    private String databaseURL = "jdbc:mysql://127.0.0.1:3306/cns?characterEncoding=UTF-8";

    private String databaseUser = "root";

    private String databasePassword = "cennavi";

    /**
     * 建表SQL
     */
    private String createSql = "";

    /**
     * 插入SQL
     */
    private String insertSql = "";
    
    /**
     * mysql 表名配置
     */
    private String tablename = "test";

    /**
     * 提交条数设置
     */
    private int batchSize = 100;

    /**
     * 线程间隔
     */
    private int threadPeriod = 10000;
    
    /**
     * 写日志的线程个数
     */
    private int hbPools = 2;

    private Connection connection = null;
    

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

    /**
     * 
     * Type:TimeMillis\Date#format\AtomicInteger
     * 
     * Example:[GISBILL_]&&[_]&&(Date<yyyy-MM-dd>).log
     * 
     * Result: GISBILL_EXTATTRVALUE_2009-04-16.log
     */
    protected String tableNameFormat;

    protected long startTime;

    protected List<FileItem> fileItmes = null;

    private InterTrigger trigger;

    /**
     * 设置写入间隔时间与batchSize 可以同时使用
     */
    protected String dateSwitch;

    protected long dateLength;

    private void initFileFormat() {
        if (ObjUtil.isEmpty(fileItmes)) {
            fileItmes = LogWriterUtil.splitFileFormat(tableNameFormat);
        }
    }

    private boolean dataSwitchIsNumber = false;

    /**
     * log4j初始设置，启动日志处理计划任务
     */
    @Override
    public void activateOptions() {
        try {
            super.activateOptions();
            // 创建一个计划任务，并自定义线程名
            executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(JDBCLogAppender.class.getName()));
            //executor = Executors.newScheduledThreadPool(hbPools,new NamedThreadFactory(JDBCLogAppender.class.getName()));
            //executor.submit(this);
            // 日志队列
            loggingEvents = new ConcurrentLinkedQueue<LoggingEvent>();
            
            // 启动计划任务，如果run函数有异常任务将中断！
            //for(int i = 0; i < hbPools;i++){
                task = executor.scheduleWithFixedDelay(this, threadPeriod, threadPeriod, TimeUnit.MILLISECONDS);
            //}
            
            // 初始化表名format
            initFileFormat();
            
            startTime = System.currentTimeMillis();

            System.out.println("ActivateOptions ok!");
        } catch (Exception e) {
            System.err.println("Error during activateOptions: " + e);
        }
    }

    @Override
    public void run() {
        //System.out.println("Execute in pool:" + Thread.currentThread().getId());
        try {
            /**
             * 是否需要执行定时操作
             */
            boolean needRoolOver = false;
            if (batchSize <= loggingEvents.size()) {
                needRoolOver = true;
                System.out.println("[" + this.getName() + "]连接行数到达最大，写入数据表。(最大值为[" + batchSize + "])");
            }

            int count = loggingEvents.size();
            if (dataSwitchIsNumber) {

                if (count > 0 && System.currentTimeMillis() >= getDateLength() + startTime) {
                    System.out.println("[" + this.getName() + "]时间间隔最大，写入数据表。(时间间隔为[" + dateSwitch + "]分钟)");
                    needRoolOver = true;
                }
            } else {
                if (count > 0 && !ObjUtil.isEmpty(trigger) && System.currentTimeMillis() >= getDateLength() + startTime) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(startTime);
                    //System.out.println("[" + this.getName() + "]时间trigger达到最大 ，写入数据表。上次切换时间： [" + BaseDate.parseDate(c.getTime(), 0) + "]， 当前时间： ["
                           // + BaseDate.parseDate(new Date(), 0) + "]，间隔长度  [" + getDateLength() + "]ms ");
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

    /**
     * <p>Discription:[获取JDBC连接]</p>
     * @return
     * @throws SQLException
     * @author:caodezhi
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    protected Connection getConnection() throws SQLException {
        if (!DriverManager.getDrivers().hasMoreElements()) {
            setDriver("com.mysql.jdbc.Driver");
        }
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DriverManager.getConnection(this.databaseURL, this.databaseUser, this.databasePassword);
            connection.setAutoCommit(false); 
        }

        return this.connection;
    }

    public void setDriver(String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (Exception e) {
            this.errorHandler.error("Failed to load driver", e, 0);
        }
    }

    protected void closeConnection(Connection con) {
    }

    private void execute(String sql) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            closeConnection(con);
        }
    }
    
    /**
     * <p>Discription:[执行日志插入db]</p>
     * @throws Exception
     * @author:caodezhi
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    private void rollOver() throws Exception {
        LoggingEvent event;
        
        /**
         * 判断表是否存在，若不存在则新建
         */
        this.tablename = this.createFileName();
        initTalbe(tablename);
        
        /**
         * 循环构建sql语句并执行操作
         */
        StringBuffer newInsert = new StringBuffer(500);
        String newInsertSql = insertSql.replace("?", tablename);
        while ((event = loggingEvents.poll()) != null) {
            try {
                //System.out.println(layout.format(event));
                newInsert.append(newInsertSql);
                newInsert.append(" values(");
                newInsert.append( layout.format(event));
                newInsert.append(")");
                //String strval = layout.format(event);
                
                //String insertLogSql = newInsertSql + " values(" + layout.format(event) + ")";
                
                execute(newInsert.toString());
                
                newInsert.setLength(0);
            } catch (Exception e) {
                System.err.println("Error logging put " + e);
            }
        }
        startTime = System.currentTimeMillis();
    }

    /**
     * <p>Discription:[初始化判断表是否存在，若不存在则创建]</p>
     * @param tempTableName
     * @throws Exception
     * @author:caodezhi
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    private void initTalbe(String tempTableName) throws Exception {
        /**
         * 判断数据表是否存在，如果不存在则创建
         */
        String sql = " SELECT table_name FROM information_schema.TABLES WHERE table_name = '" + tempTableName + "'  ";
        
        ResultSet rs = null;
        Connection con = null;
        Statement stmt = null;

        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                String newCreateSql = createSql.replace("?", tempTableName);
                execute(newCreateSql);
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if(rs != null){
                rs.close();
            }
            closeConnection(con);
        }
    }

    /**
     * 日志事件
     * 
     * @param loggingEvent
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
        try {
            // 添加到日志队列
            loggingEvents.add(loggingEvent);
        } catch (Exception e) {
            System.err.println("Error populating event and adding to queue" + e);
        }
    }

    @Override
    public void close() {
        try{
            if (task != null) {
                task.cancel(false);
            }
            if (executor != null) {
                executor.shutdown();
            }
            
            if ((this.connection != null) && (!this.connection.isClosed())){
              this.connection.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing connection " + e);
        }
        this.closed = true;
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public void settableNameFormat(String fileFormat) {
        if (ObjUtil.isEmpty(fileFormat)) {
            fileFormat = "[DEFAULT_LOG_]%%(TimeMillis)%%[_]%%(AtomicInteger)";
        }
        this.tableNameFormat = fileFormat;
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
            this.setDateLength(new Integer(this.getDateSwitch()) * 60 * 1000);
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
        //System.out.println("时间间隔为 [" + temp + "], endTime : [" + BaseDate.parseDate(endTime.getTime(), 0) + "],startTime : [" + BaseDate.parseDate(startTime.getTime(), 0) + "]");
        while (temp < -0) {
            temp = getSwitchTime(trigger);
        }
        return temp;
    }

    private String createFileName() {
        StringBuffer str = new StringBuffer();
        if (fileItmes != null) {
            for (int i = 0; i < fileItmes.size(); i++) {
                str.append(fileItmes.get(i).getValue(this));
            }
            return str.toString();
        } else {
            return "DEFAULT_LOG";
        }
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
    
    public String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getCreateSql() {
        return createSql;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }
    
    public String getTablename() {
        return tablename;
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

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getTableNameFormat() {
        return tableNameFormat;
    }
    
}
