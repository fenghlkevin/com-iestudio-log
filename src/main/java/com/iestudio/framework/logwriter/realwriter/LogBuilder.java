package com.iestudio.framework.logwriter.realwriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.LevelRangeFilter;

import com.iestudio.framework.logwriter.appender.RollingTimerMoveFileAppender;
import com.iestudio.framework.logwriter.filter.PackageFilter;

/**
 * Created on 2011-12-23
 * <p>Title: WEB-T GIS核心系统_日志_动态构建日志模块</p>
 * <p>Copyright: Copyright (c) 2011</p>
 * <p>Company: 沈阳世纪高通科技有限公司</p>
 * <p>Department: 技术开发部</p>
 * 
 * @author Kevin Feng fengheliang@cennavi.com.cn
 * @version 1.0
 * @update 修改日期 修改描述
 */
public class LogBuilder {

    /**
     * <p>Discription:[初始化模块Appender]</p>
     * @param moduleName
     * @author:Kevin Feng
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public void loadLog(String moduleName, String templateLoggerStr, String templateAppenderStr, String logPath, String targetPath, Level maxLevel,
            Level minLevel, String hodpackage) {
        Logger debugLogger = Logger.getLogger(moduleName);

        if (debugLogger.getAppender(moduleName) == null) {
            System.out.println("创建GIS LOG APPENDER : ["+moduleName+"]");

            Logger templateLogger = null;
            if (templateLoggerStr != null && !"".equalsIgnoreCase(templateLoggerStr.trim())) {
                templateLogger = Logger.getLogger(templateLoggerStr);
            } else {
                templateLogger = Logger.getRootLogger();
            }

            RollingTimerMoveFileAppender templateAppender = (RollingTimerMoveFileAppender) templateLogger.getAppender(templateAppenderStr);
            RollingTimerMoveFileAppender appender = new RollingTimerMoveFileAppender();

            appender.setName(moduleName);
            appender.setAppend(templateAppender.getAppend());
            appender.setDateSwitch(templateAppender.getDateSwitch());
            appender.setMaxCount(templateAppender.getMaxCount());
            appender.setFileFormat("[" + moduleName + "_]" + templateAppender.getFileFormat());
            appender.setMaxFileSize(templateAppender.getMaxFileSize());
            appender.setFile(templateAppender.getFile());
            appender.setLogPath(logPath);
            appender.setTargetPath(targetPath);
            appender.setRegularExpressions("^" + moduleName);

            if (templateAppender.getLayout() != null) {
                appender.setLayout(templateAppender.getLayout());
            }

            PackageFilter filter = new PackageFilter();
            filter.setHoldPackage(hodpackage);
            filter.activateOptions();
            LevelRangeFilter lrfilter = new LevelRangeFilter();
            filter.setNext(lrfilter);
            lrfilter.setAcceptOnMatch(true);
            lrfilter.setLevelMax(maxLevel);
            lrfilter.setLevelMin(minLevel);
            lrfilter.activateOptions();
            appender.addFilter(filter);
            debugLogger.addAppender(appender);
            appender.activateOptions();
        }
    }

    public Level getStrLevel(String logLevel) {
        if (logLevel == null || "".equalsIgnoreCase(logLevel)) {
            return Level.OFF;
        }
        Field[] fields = Level.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && logLevel.equalsIgnoreCase(f.getName())) {
                Object level = null;
                try {
                    level = f.get(null);
                } catch (IllegalArgumentException e) {
                    System.out.println("获取Log Level时异常");
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    System.out.println("获取Log Level时异常");
                    e.printStackTrace();
                }
                if (level instanceof Level) {
                    return (Level) level;
                }
            }
        }
        return Level.OFF;
    }

}
