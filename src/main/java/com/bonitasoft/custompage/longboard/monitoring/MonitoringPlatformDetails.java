package com.bonitasoft.custompage.longboard.monitoring;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.bonitasoft.custompage.longboard.toolbox.LongboardToolbox;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;

public class MonitoringPlatformDetails {

    public final static String loggerName = MonitoringPlatformDetails.class.getName();
    public static Logger logger = Logger.getLogger(loggerName);

    public static HashMap<String, Object> getDetails(boolean detailsError, PlatformMonitoringAPI platformMonitoringAPI) {
        HashMap<String, Object> mapMonitoring = new HashMap<String, Object>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

        try {
            mapMonitoring.put("AvailableProcessor", platformMonitoringAPI.getAvailableProcessors());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("JvmName", platformMonitoringAPI.getJvmName());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("MemUsage", (int) (platformMonitoringAPI.getCurrentMemoryUsage() / 1024));
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("MemFree", (int) (platformMonitoringAPI.getFreePhysicalMemorySize() / 1024));
        } catch (Exception e) {
            mapMonitoring.put("MemFree", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("MemFreeSwap", platformMonitoringAPI.getFreeSwapSpaceSize());
        } catch (Exception e) {
            mapMonitoring.put("MemFreeSwap", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("MemTotalPhysicalMemory", platformMonitoringAPI.getTotalPhysicalMemorySize());
        } catch (Exception e) {
            mapMonitoring.put("MemTotalPhysicalMemory", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("MemTotalSwapSpace", platformMonitoringAPI.getTotalSwapSpaceSize());
        } catch (Exception e) {
            mapMonitoring.put("MemTotalSwapSpace", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("JavaFreeMemory", (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024));
        } catch (Exception e) {
            mapMonitoring.put("JavaFreeMemory", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("JavaTotalMemory", (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024));
        } catch (Exception e) {
            mapMonitoring.put("JavaTotalMemory", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("JavaUsedMemory", (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        } catch (Exception e) {
            mapMonitoring.put("JavaUsedMemory", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("JvmSystemProperties", platformMonitoringAPI.getJvmSystemProperties());
        } catch (Exception e) {
            mapMonitoring.put("JvmSystemProperties", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("JvmVendor", platformMonitoringAPI.getJvmVendor());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("JvmVersion", platformMonitoringAPI.getJvmVersion());
        } catch (Exception e) {
        } ;
        /*
         * try { Map<String,GcInfo> gcInfo =
         * platformMonitoringAPI.getLastGcInfo(); String gcInfoReturn = ""; for
         * (String key : gcInfo.keySet()) { gcInfoReturn+=":MemBeforeGc:"+
         * gcInfo.get(key).getMemoryUsageBeforeGc() +
         * ",MemAfterGc:"+gcInfo.get(key).getMemoryUsageAfterGc()
         * +",Duration:"+gcInfo.get(key).getDuration()
         * +",StartTime:"+simpleDateFormat.format( new
         * Date(gcInfo.get(key).getStartTime())); break; // only one }
         * mapMonitoring.put("LastGCInfo",gcInfoReturn); } catch (Exception e) {
         * mapMonitoring.put("LastGCInfo",detailsError ?
         * "error ["+e.toString()+"]":"Not Implemented"); };
         */
        mapMonitoring.put("LastGCInfo", "");

        try {
            mapMonitoring.put("MemUsagePercentage", (int) platformMonitoringAPI.getMemoryUsagePercentage());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("NumberActiveTransaction", platformMonitoringAPI.getNumberOfActiveTransactions());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("OSArch", platformMonitoringAPI.getOSArch());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("OSName", platformMonitoringAPI.getOSName());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("OSVersion", platformMonitoringAPI.getOSVersion());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("ProcessCPUTime", platformMonitoringAPI.getProcessCpuTime());
        } catch (Exception e) {
            mapMonitoring.put("ProcessCPUTime", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("StartTime", platformMonitoringAPI.getStartTime());
        } catch (Exception e) {
            mapMonitoring.put("StartTime", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("StartTimeHuman", simpleDateFormat.format(new Date(platformMonitoringAPI.getStartTime())));
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("LoadAverageLastMn", platformMonitoringAPI.getSystemLoadAverage());
        } catch (Exception e) {
            mapMonitoring.put("LoadAverageLastMn", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            mapMonitoring.put("ThreadCount", platformMonitoringAPI.getThreadCount());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("TotalThreadsCpuTime", platformMonitoringAPI.getTotalThreadsCpuTime());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("UpTime", platformMonitoringAPI.getUpTime());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("IsSchedulerStarted", platformMonitoringAPI.isSchedulerStarted());
        } catch (Exception e) {
        } ;
        try {
            mapMonitoring.put("CommitedVirtualMemorySize", platformMonitoringAPI.getCommittedVirtualMemorySize());
        } catch (Exception e) {
            mapMonitoring.put("CommitedVirtualMemorySize", detailsError ? "error [" + e.toString() + "]" : "Not Implemented");
        } ;
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            List<String> jvmArgs = runtimeMXBean.getInputArguments();
            String listJvmArgs = "";
            for (String arg : jvmArgs) {
                listJvmArgs += arg + " ";
            }
            mapMonitoring.put("JVMArgs", listJvmArgs);
        } catch (Exception e) {
            mapMonitoring.put("JVMArgs", "Error [" + e.toString() + "]");
            logger.severe("JVM ARG[" + e.toString() + "]");
        } ;

        Connection con = null;
        try {
            // Now access the database
            con = LongboardToolbox.getConnection();

            if (con == null) {
                mapMonitoring.put("errormessage", "Can't access the database using datasource [java:/comp/env/bonitaSequenceManagerDS] or [java:jboss/datasources/bonitaSequenceManagerDS]");
            } else {
                DatabaseMetaData databaseMetaData = con.getMetaData();
                mapMonitoring.put("DatabaseMajorVersion", databaseMetaData.getDatabaseMajorVersion());
                mapMonitoring.put("DatabaseMinorVersion", databaseMetaData.getDatabaseMinorVersion());
                mapMonitoring.put("DatabaseProductName", databaseMetaData.getDatabaseProductName());
                mapMonitoring.put("DatabaseProductVersion", databaseMetaData.getDatabaseProductVersion());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();

            logger.severe("#### LongBoardCustomPage:Groovy getMonitoringApiInJson error[" + e.toString() + "] at " + exceptionDetails);
            mapMonitoring.put("errormessage", e.toString());

            if (con != null)
                try {
                    con.close();
                } catch (Exception e2) {
                } ;
        } ;

        return mapMonitoring;
    }

}
