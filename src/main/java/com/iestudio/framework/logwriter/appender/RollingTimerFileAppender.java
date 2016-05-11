package com.iestudio.framework.logwriter.appender;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

import com.iestudio.object.ObjUtil;
import com.iestudio.trigger.CronTrigger;
import com.iestudio.trigger.InterTrigger;
import com.iestudio.date.BaseDate;
import com.iestudio.framework.logwriter.realwriter.LogWriter;
import com.iestudio.framework.logwriter.util.LogWriterUtil;
import com.iestudio.framework.logwriter.util.LogWriterUtil.FileItem;

public class RollingTimerFileAppender extends FileAppender {

    // private Log loger = LogFactory.getLog(RollingTimerFileAppender.class);

    protected int maxCount;

    protected String dateSwitch;

    /**
     * 删除了DefineVariable [Constant]&&(Type) //&&{DefineVariable}
     * 
     * Type:TimeMillis\Date#format\AtomicInteger
     * 
     * Example:[GISBILL_]&&[_]&&(Date<yyyy-MM-dd>).log
     * 
     * Result: GISBILL_EXTATTRVALUE_2009-04-16.log
     */
    protected String fileFormat;

    protected long startTime;

    protected long dateLength;

    protected String logPath;

    protected long maxFileSize;

    protected List<FileItem> fileItmes = null;

    private InterTrigger trigger;

    private String lastFileName = "";

    public RollingTimerFileAppender() {
    }

    protected void setQWForFiles(Writer writer) {
        qw = new LogWriter(writer, errorHandler);
    }

    private void initFileFormat() {
        if (ObjUtil.isEmpty(fileItmes)) {
            fileItmes = LogWriterUtil.splitFileFormat(fileFormat);
        }
    }

    private boolean dataSwitchIsNumber = false;

    protected void subAppend(LoggingEvent event) {
        if (fileName != null) {
            boolean needRoolOver = false;

            if ((maxCount > 0 && ((LogWriter) qw).getRowCount() >= maxCount)) {
                System.out.println("[" + this.getName() + "]连接行数到达最大，切换文件。(最大值为[" + maxCount + "])");
                needRoolOver = true;
            }

            if (dataSwitchIsNumber) {
                int count = new Integer(this.dateSwitch);
                if (count > 0 && System.currentTimeMillis() >= getDateLength() + startTime) {
                    System.out.println("[" + this.getName() + "]时间间隔最大，切换文件。(时间间隔为[" + dateSwitch + "]分钟)");
                    needRoolOver = true;
                }
            } else {
                if (!ObjUtil.isEmpty(trigger) && System.currentTimeMillis() >= getDateLength() + startTime) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(startTime);
                    System.out.println("[" + this.getName() + "]时间trigger达到最大 ，切换文件。上次切换时间： ["
                            + BaseDate.parseDate(c.getTime(), 0) + "]， 当前时间： [" + BaseDate.parseDate(new Date(), 0) + "]，间隔长度  [" + getDateLength() + "]ms ");
                    needRoolOver = true;
                }
            }

            if (maxFileSize > -1 && ((CountingQuietWriter) qw).getCount() >= maxFileSize) {
                System.out.println("[" + this.getName() + "]文件大小达到最大，切换文件。(最大文件大小为[" + maxFileSize + "])");
                needRoolOver = true;
            }

            if (needRoolOver) {
                rollOver();
                // 当trigger过期时，才进行再次获取时间 && System.currentTimeMillis() >= getDateLength() + startTime
                if (!ObjUtil.isEmpty(trigger)) {
                    this.setDateLength(this.getSwitchTime(trigger));
                }
            }

            // if ((maxCount > 0 && ((LogWriter) qw).getRowCount() >= maxCount)
            // || (!"-1".equalsIgnoreCase(dateSwitch)
            // && (!ObjUtil.isEmpty(dateSwitch) && ObjUtil.isNumber(dateSwitch)
            // ? new Integer(dateSwitch) > 0 : !ObjUtil.isEmpty(trigger)) &&
            // System
            // .currentTimeMillis() >= getDateLength() + startTime) ||
            // (maxFileSize > 0 && ((CountingQuietWriter) qw).getCount() >=
            // maxFileSize)) {
            // rollOver();
            // if (!ObjUtil.isEmpty(trigger)) {
            // this.setDateLength(this.getSwitchTime(trigger));
            // }
            // }
        }
        super.subAppend(event);
    }

    public void rollOver() {
        // LogLog.debug("开始：切换文件");
        // LogLog.debug("关闭文件系统");

        String fileNameT = this.createFileName();
        if (this.lastFileName.equalsIgnoreCase(fileNameT)) {
            startTime = System.currentTimeMillis();
            return;
        }

        super.closeFile();
        // LogLog.debug("操作：doAfterCloseFile()");
        doAfterCloseFile();
        this.fileName = this.getLogPath() + "/" + fileNameT;
        // LogLog.debug("操作：设置文件名 [" + fileName + "]");
        // File f = new File(fileName);
        // int existsFile = 0;
        // String temp = fileName;
        // while (f.exists()) {
        // temp = this.fileName + existsFile++;
        // f = new File(temp);
        // }
        // fileName = temp;
        // LogLog.debug("操作：最终获取文件名[" + fileName + "]");
        // LogLog.debug("操作：切换文件");
        try {
            setFile(fileName, true, bufferedIO, bufferSize);
        } catch (IOException e) {
            // LogLog.error("setFile(" + fileName + ", false) call failed.", e);
        }
        startTime = System.currentTimeMillis();
        this.lastFileName = fileNameT;
        // LogLog.debug("结束：切换文件");
    }

    protected String createFileName() {
        StringBuffer str = new StringBuffer();
        if (fileItmes != null) {
            for (int i = 0; i < fileItmes.size(); i++) {
                str.append(fileItmes.get(i).getValue(this));
            }
            return str.toString();
        } else {
            return "DEFAULT.log";
        }
    }

    public long getMaximumFileSize() {
        return maxFileSize;
    }

    private String maxFileSizeStr;
    public void setMaxFileSize(String value) {
        maxFileSizeStr=value;
        maxFileSize = OptionConverter.toFileSize(maxFileSizeStr, maxFileSize + 1L);
    }
    public String getMaxFileSize() {
        return maxFileSizeStr;
    }

    public long getDateLength() {
        return dateLength;
    }

    public void setDateLength(long dateLength) {
        this.dateLength = dateLength;
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
        System.out.println("时间间隔为 ["+temp+"], endTime : ["+BaseDate.parseDate(endTime.getTime(),0)+"],startTime : ["+BaseDate.parseDate(startTime.getTime(),0)+"]");
        while (temp < -0) {
            temp = getSwitchTime(trigger);
        }
        // System.out.println("nextFireTime : [" + new
        // SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextFireTime) + "]");
        return temp;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        if (ObjUtil.isEmpty(fileFormat)) {
            fileFormat = "[DEFAULT_LOG_]%%(TimeMillis)%%[_]%%(AtomicInteger)%%[.log]";
        }
        this.fileFormat = fileFormat;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    protected void doBeforeActivate() {
    }

    protected void doAfterCloseFile() {
    }

    public void activateOptions() {
        // LogLog.debug("开始：初始化日志信息");
        // LogLog.debug("判断日志文件夹");
        if (ObjUtil.isEmpty(this.logPath)) {
            throw new RuntimeException("logPath为空，不能执行日志操作");
        } else {
            File pa = new File(this.logPath);
            if (!pa.exists()) {
                pa.mkdirs();
            } else {
                if (!pa.isDirectory()) {
                    // LogLog.error("logPath  [" + logPath +
                    // "]  不是文件夹，生成的文件不能发到指定目录");
                    pa.mkdirs();
                }
            }
        }
        // LogLog.debug("操作：初始化SHUTDOWN方法");
        doShutDownWork();
        // LogLog.debug("操作：初始化文件名Format");
        initFileFormat();
        startTime = System.currentTimeMillis();
        String fileNameT = this.createFileName();
        this.fileName = this.getLogPath() + "//" + fileNameT;
        this.lastFileName = fileNameT;
        // 初始化文件名后再进行删除操作，这样当有上次服务停止使用的文件与这次启动相同的话，可以避免被移动走，从而引起文件重名问题
        // LogLog.debug("操作：doBeforeActivate()");
        this.doBeforeActivate();
        // LogLog.debug("操作：设置文件名  [" + this.fileName + "]");
        super.activateOptions();
        // LogLog.debug("结束：初始化日志信息");
    }

    protected void doBeforeShutDown() {
    }

    private void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                doBeforeShutDown();
            }
        });
    }
}
