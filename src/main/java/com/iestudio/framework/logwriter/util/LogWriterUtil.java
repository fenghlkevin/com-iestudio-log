package com.iestudio.framework.logwriter.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.iestudio.framework.logwriter.constants.LogContants;
import com.kevin.iesutdio.tools.clazz.ObjUtil;
import com.kevin.iesutdio.tools.clazz.ObjectUtil;

public class LogWriterUtil {

    public static List<FileItem> splitFileFormat(String format) {
        List<FileItem> items = new ArrayList<FileItem>();

        String[] fileNames = format.split("%%");

        LogWriterUtil u=new LogWriterUtil();
        String oneItem="";
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].indexOf("[") >= 0
                    && fileNames[i].indexOf("]") >= 0) {
                oneItem = fileNames[i].replaceAll("\\[", "").replaceAll("\\]", "");
                items.add(u.new FileItem(LogContants.TYPE_CONSTANT, oneItem));

            } else if (fileNames[i].indexOf("{") >= 0
                    && fileNames[i].indexOf("}") >= 0) {
                oneItem = fileNames[i].replaceAll("\\{", "").replaceAll("\\}", "");
                items.add(u.new FileItem(LogContants.TYPE_EXT, oneItem));

            } else if (fileNames[i].indexOf("(") >= 0
                    && fileNames[i].indexOf(")") >= 0) {
                oneItem = fileNames[i].replaceAll("\\(", "").replaceAll("\\)", "");
                
                if(oneItem.toUpperCase().contains(LogContants.TYPE_TIMEMILLIS.toUpperCase())){
                    items.add(u.new FileItem(LogContants.TYPE_TIMEMILLIS, oneItem));
                }else if(oneItem.toUpperCase().contains(LogContants.TYPE_DATE.toUpperCase())){
                    items.add(u.new FileItem(LogContants.TYPE_DATE, oneItem,oneItem.substring(oneItem.indexOf("#")+1)));
                }else if(oneItem.toUpperCase().contains(LogContants.TYPE_ATOMICINTEGER.toUpperCase())){
                    items.add(u.new FileItem(LogContants.TYPE_ATOMICINTEGER,new AtomicInteger(0)));
                }
            }
        }
        return items;
    }

    public final class FileItem {

        public FileItem() {
        }
        
        public FileItem(String type) {
            this.type = type;
        }

        public FileItem(String type, Object value) {
            this.type = type;
            this.value = value;
        }

        public FileItem(String type, Object value, String formatType) {
            this.type = type;
            this.value = value;
            this.formattype = formatType;
        }

        private String type;

        private Object value;

        private String formattype;

        public String getFormattype() {
            return formattype;
        }

        public void setFormattype(String formattype) {
            this.formattype = formattype;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue(Object valueObj) {
            if (this.type.equalsIgnoreCase(LogContants.TYPE_CONSTANT)) {
                return (String) value;
            }else if(this.type.equalsIgnoreCase(LogContants.TYPE_ATOMICINTEGER)){
                return String.valueOf(((AtomicInteger)value).incrementAndGet());
            } else if (this.type.equalsIgnoreCase(LogContants.TYPE_TIMEMILLIS)) {
                return String.valueOf(System.currentTimeMillis());
            } else if (this.type.equalsIgnoreCase(LogContants.TYPE_DATE)) {
                if (ObjUtil.isEmpty(this.getFormattype())) {
                    throw new RuntimeException(
                            "文件名中有Date对象，但是没有配置formattype");
                }
                SimpleDateFormat s = new SimpleDateFormat(this.getFormattype());
                return s.format(new Date());
            } else if (this.type.equalsIgnoreCase(LogContants.TYPE_EXT)) {
                try {
                    return (String) ObjectUtil.getMethodByName(valueObj,"get"+(String) this.value,false).invoke(valueObj);
//                    return (String) Util.getGetMethodByName(valueObj,
//                            (String) this.value).invoke(valueObj);
                } catch (Exception e) {
                    throw new RuntimeException("FileItem获取真实值，反射时异常", e);
                }
            } else {
                return "";
            }

        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
