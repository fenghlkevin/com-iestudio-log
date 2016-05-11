package com.iestudio.framework.logwriter.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class PackageFilter extends Filter {

    // 保留包
    private List<String> holdPackageList;

    // 过滤包
    private List<String> filteringPackageList;

    // 忽略包
    private List<String> ignorePackageList;

    // 保留包
    private String holdPackage;

    //过滤包
    private String filteringPackage;

    // 忽略包
    private String ignorePackage;

    private Pattern holdPattern;

    private Pattern filteringPattern;

    private Pattern ignorePattern;

    public PackageFilter() {
        holdPackageList = new ArrayList<String>();
        filteringPackageList = new ArrayList<String>();
        ignorePackageList = new ArrayList<String>();
    }

    @Override
    public int decide(LoggingEvent event) {
        if ((this.ignorePattern != null) && this.ignorePattern.matcher(event.getLoggerName()).find()) {
            return Filter.NEUTRAL;
        }

        if (this.holdPattern != null && this.holdPattern.matcher(event.getLoggerName()).find()) {
            return Filter.ACCEPT;
        }

        if (this.filteringPattern != null && this.filteringPattern.matcher(event.getLoggerName()).find()) {
            return Filter.DENY;
        }

        return Filter.NEUTRAL;
    }

    private Pattern createpattern(List<String> packageStr) {
        StringBuffer str = new StringBuffer(50);
        Pattern p = null;
        if (packageStr != null && packageStr.size() > 0) {
            int i = 0;
            for (String holdStr : packageStr) {
                str.append("^");
                str.append(holdStr);
                if (i < packageStr.size() - 1) {
                    str.append("|");
                }
                i++;
            }
            p = Pattern.compile(str.toString());
            str = null;
        }
        return p;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        this.holdPattern = createpattern(this.holdPackageList);
        this.ignorePattern = createpattern(this.ignorePackageList);
        this.filteringPattern = createpattern(this.filteringPackageList);
        this.filteringPackage = null;
        this.ignorePackage = null;
        this.holdPackage = null;

    }

    public void setHoldPackage(String holdPackage) {
        this.holdPackageList.add(holdPackage);
    }

    public void setFilteringPackage(String filteringPackage) {
        this.filteringPackageList.add(filteringPackage);
    }

    public void setIgnorePackage(String ignorePackage) {
        this.ignorePackageList.add(ignorePackage);
    }

    public String getHoldPackage() {
        return holdPackage;
    }

    public String getFilteringPackage() {
        return filteringPackage;
    }

    public String getIgnorePackage() {
        return ignorePackage;
    }

}
