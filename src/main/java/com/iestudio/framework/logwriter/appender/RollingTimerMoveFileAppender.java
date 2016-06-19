package com.iestudio.framework.logwriter.appender;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import com.iestudio.file.FileUtil;
import com.kevin.iesutdio.tools.clazz.ObjUtil;

public class RollingTimerMoveFileAppender extends RollingTimerFileAppender {

    private String targetPath;

    protected boolean needMoveFiles = false;

    private String regularExpressions;

    public String getRegularExpressions() {
        return regularExpressions;
    }

    public void setRegularExpressions(String regularExpressions) {
        this.regularExpressions = regularExpressions;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Override
    protected void doAfterCloseFile() {
        super.doAfterCloseFile();
        if (needMoveFiles) {
            //LogLog.debug("移动文件到 [" + targetPath + "] 目录");
            //LogLog.debug("开始：[" + fileName + "] 文件关闭，进行所有文件移动");
            FileUtil.moveFilesAndDelete(this.logPath, this.targetPath, getInstance(getRegularExpressions(), null), true, true);
            //LogLog.debug("结束：[" + fileName + "] 文件关闭，进行所有文件移动");
        } else {
            //LogLog.debug("targetPath为空，不进行文件移动");
        }

    }

    @Override
    protected void doBeforeActivate() {
        super.doBeforeActivate();
        if (needMoveFiles) {
            //LogLog.debug("移动文件到 [" + targetPath + "] 目录");
            //LogLog.debug("开始：第一次启动，进行所有文件移动");
            FileUtil.moveFilesAndDelete(this.logPath, this.targetPath, getInstance(getRegularExpressions(), this.fileName), true, true);
            //LogLog.debug("结束：第一次启动，进行所有文件移动");
        } else {
            //LogLog.debug("targetPath为空，不进行文件移动");
        }
    }

    /**
     * 主要用作继承类，目标文件夹不是本地文件夹时，建立本地无用目录问题
     */
    protected boolean useLocalTargetPath = true;

    @Override
    public void activateOptions() {
        needMoveFiles = true;
        if (useLocalTargetPath) {
            if (ObjUtil.isEmpty(targetPath)) {
                needMoveFiles = false;
                //LogLog.error("[LOG4J]  目标对象为空，生成的文件不能转移到指定目录");
            } else {
                needMoveFiles = true;
                File pa = new File(targetPath);
                if (!pa.exists()) {
                    pa.mkdirs();
                } else {
                    if (!pa.isDirectory()) {
                        //LogLog.error("targetPath  [" + targetPath + "]  不是文件夹，生成的文件不能转移到指定目录");
                        pa.mkdirs();
                    }
                }
            }
        }
        super.activateOptions();
    }

    @Override
    protected void doBeforeShutDown() {
        super.doBeforeShutDown();
        // super.closeFile();
        // if (needMoveFiles) {
        // LogLog.debug("移动文件到 [" + targetPath + "] 目录");
        // LogLog.debug("开始：关闭前，进行所有文件移动");
        // FileUtil.moveFilesAndDelete(this.logPath, this.targetPath,
        // getInstance(getRegularExpressions(), this.fileName), true);
        // LogLog.debug("结束：关闭前，进行所有文件移动");
        // } else {
        // LogLog.debug("targetPath为空，不进行文件移动");
        // }
    }

    protected FileFilter getInstance(String regularExpressions, String fileName) {
        FileFilter filter = null;
        if (ObjUtil.isEmpty(filter)) {
            if (ObjUtil.isEmpty(fileName)) {
                filter = new DefaultFileFilter(regularExpressions);
            } else {
                filter = new DefaultFileFilter(regularExpressions, fileName);
            }

        }
        return filter;
    }

    private class DefaultFileFilter implements FileFilter {

        private Pattern pattern;

        private DefaultFileFilter(String regularExpressions) {
            if (!ObjUtil.isEmpty(regularExpressions)) {
                pattern = Pattern.compile(regularExpressions);
            }
        }

        private String fileName;

        private DefaultFileFilter(String regularExpressions, String fileName) {
            this(regularExpressions);
            this.fileName = fileName;
        }

        public boolean accept(File file) {
            String fileName = file.getName();
            if (ObjUtil.isEmpty(fileName)) {
                return false;
            }

            if (!ObjUtil.isEmpty(pattern) && !pattern.matcher(fileName).find()) {
                return false;
            }

            if (!ObjUtil.isEmpty(this.fileName) && this.fileName.endsWith(fileName)) {
                return false;
            }

            return true;
        }
    }

}
