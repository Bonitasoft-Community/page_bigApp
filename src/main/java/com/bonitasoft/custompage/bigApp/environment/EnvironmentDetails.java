package com.bonitasoft.custompage.bigApp.environment;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EnvironmentDetails extends HttpServlet {

    static String catalinaBase = System.getProperty( "catalina.base" );
    static String jBossBase = System.getProperty( "jboss.server.config.dir" );

    public static Map<String, Object> getEnvironment(APISession session) throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException, FileNotFoundException, UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        Map<String, Object> result = new HashMap<String, Object>();

        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI( session );

        // Prepare the data that will be displayed on the page

        result.put( "operatingSystemInfos", platformMonitoringAPI.getOSName() + " - " + platformMonitoringAPI.getOSVersion() );

        result.put( "javaMachine", platformMonitoringAPI.getJvmName() + " " + platformMonitoringAPI.getJvmVersion() );

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method getTotalPhysicalMemorySizeMethod = operatingSystemMXBean.getClass().getMethod("getTotalPhysicalMemorySize", null);
        getTotalPhysicalMemorySizeMethod.setAccessible(true);
        Object totalPhysicalMemorySizeValue = getTotalPhysicalMemorySizeMethod.invoke(operatingSystemMXBean);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        result.put( "MemTotalPhysicalMemory", df.format(Double.valueOf(totalPhysicalMemorySizeValue.toString()) / (1024 * 1024 * 1024)) );

        df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        result.put( "memoryUsage", df.format( platformMonitoringAPI.getMemoryUsagePercentage()) + " % " );

        if (catalinaBase != null && StringUtils.containsIgnoreCase( catalinaBase, "Tomcat" )) {

            String pathwebserver = catalinaBase;
            int indexBonitaStart = pathwebserver.indexOf( "tomcat" );
            if (null != pathwebserver && pathwebserver.indexOf( "tomcat" ) == -1 && pathwebserver.indexOf( "Tomcat" ) != -1) {

                indexBonitaStart = pathwebserver.indexOf( "Tomcat" );
                String webserver = pathwebserver.substring( indexBonitaStart );
                int indexBonitaEnd = webserver.indexOf( "/" );
                webserver = webserver.substring( 0, indexBonitaEnd );
                if (webserver != null && !webserver.isEmpty()) {
                    result.put( "WebServer", StringUtils.capitalize( webserver ) );
                } else {
                    result.put( "WebServer", getWebserverVersion() );
                }
            } else {
                result.put( "WebServer", getWebserverVersion() );
            }
        } else if (jBossBase != null && StringUtils.containsIgnoreCase( jBossBase, "wildfly" )) {
            String pathwebserver = jBossBase;
            int indexBonitaStart = pathwebserver.indexOf( "wildfly" );
            String webserver = pathwebserver.substring( indexBonitaStart );
            int indexBonitaEnd = webserver.indexOf( "/" );
            webserver = webserver.substring( 0, indexBonitaEnd );
            if (webserver != null && !webserver.isEmpty()) {
                result.put( "WebServer", StringUtils.capitalize( webserver ) );
            } else {
                result.put( "WebServer", "Wildfly" );
            }
        } else {
            result.put( "WebServer", getWebserverVersion() );
        }

        return result;
    }

    public static String getEnvironmentInfosExport(APISession session, File setupFile) throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException, UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        String result = "";

        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI( session );

        // Prepare the data that will be available for download

        result = result.concat( "Parameter;value;\n" );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        result = result.concat( "SystemDateUTC;" + dtf.format(now) + ";\n" );

        result = result.concat( "OperatingSystemInfos;" + platformMonitoringAPI.getOSName() + " - " + platformMonitoringAPI.getOSVersion() + ";\n" );

        if (System.getProperty( "catalina.base" ) != null && System.getProperty( "catalina.base" ).contains( "tomcat" )) {
            result = result.concat( "WebServer;" + "Tomcat" + ";\n" );
        } else if (System.getProperty( "jboss.server.config.dir" ) != null && System.getProperty( "jboss.server.config.dir" ).contains( "wildfly" )) {
            result = result.concat( "WebServer;" + "Wildfly" + ";\n" );
        }

        result = result.concat( "JavaMachine;" + platformMonitoringAPI.getJvmName() + " " + platformMonitoringAPI.getJvmVersion() + ";\n" );

        result = result.concat( "MemoryUsage;" + platformMonitoringAPI.getMemoryUsagePercentage() + " % " + ";\n" );

        result = result.concat( "AvailableProcessors;" + platformMonitoringAPI.getAvailableProcessors() + ";\n" );

        result = result.concat( "MemUsage;" + platformMonitoringAPI.getCurrentMemoryUsage() / 1024 + ";\n" );

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method getFreePhysicalMemorySizeMethod = operatingSystemMXBean.getClass().getMethod("getFreePhysicalMemorySize", null);
        getFreePhysicalMemorySizeMethod.setAccessible(true);
        Object FreePhysicalMemorySizeValue = getFreePhysicalMemorySizeMethod.invoke(operatingSystemMXBean);

        result = result.concat( "FreePhysicalMemorySize;" + FreePhysicalMemorySizeValue + ";\n" );

        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method getFreeSwapSpaceSizeMethod = operatingSystemMXBean.getClass().getMethod("getFreeSwapSpaceSize", null);
        getFreeSwapSpaceSizeMethod.setAccessible(true);
        Object freeSwapSpaceSizeValue = getFreeSwapSpaceSizeMethod.invoke(operatingSystemMXBean);

        result = result.concat( "MemFreeSwap;" + freeSwapSpaceSizeValue + ";\n" );

        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method getTotalPhysicalMemorySizeMethod = operatingSystemMXBean.getClass().getMethod("getTotalPhysicalMemorySize", null);
        getTotalPhysicalMemorySizeMethod.setAccessible(true);
        Object TotalPhysicalMemorySizeValue = getTotalPhysicalMemorySizeMethod.invoke(operatingSystemMXBean);

        result = result.concat( "MemTotalPhysicalMemory;" + TotalPhysicalMemorySizeValue + ";\n" );

        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        Method getTotalSwapSpaceSize = operatingSystemMXBean.getClass().getMethod("getTotalSwapSpaceSize", null);
        getTotalSwapSpaceSize.setAccessible(true);
        Object totalSwapSpaceSizeValue = getTotalPhysicalMemorySizeMethod.invoke(operatingSystemMXBean);

        result = result.concat( "MemTotalSwapSpace;" + totalSwapSpaceSizeValue + ";\n" );

        result = result.concat( "JavaFreeMemory;" + Runtime.getRuntime().freeMemory() / 1024 / 1024 + ";\n" );

        result = result.concat( "JavaTotalMemory;" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + ";\n" );

        result = result.concat( "JavaUsedMemory;" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + ";\n" );

        result = result.concat( "JvmVendor;" + platformMonitoringAPI.getJvmVendor() + ";\n" );

        result = result.concat( "JvmVersion;" + platformMonitoringAPI.getJvmVersion() + ";\n" );

        result = result.concat( "MemUsagePercentage;" + platformMonitoringAPI.getMemoryUsagePercentage() + ";\n" );

        result = result.concat( "NumberActiveTransaction;" + platformMonitoringAPI.getNumberOfActiveTransactions() + ";\n" );

        result = result.concat( "OSArch;" + platformMonitoringAPI.getOSArch() + ";\n" );

        result = result.concat( "OSName;" + platformMonitoringAPI.getOSName() + ";\n" );

        result = result.concat( "OSVersion;" + platformMonitoringAPI.getOSVersion() + ";\n" );

        result = result.concat( "StartTime;" + platformMonitoringAPI.getStartTime() + ";\n" );

        result = result.concat( "LoadAverageLastMn;" + platformMonitoringAPI.getSystemLoadAverage() + ";\n" );

        result = result.concat( "ThreadCount;" + platformMonitoringAPI.getThreadCount() + ";\n" );

        result = result.concat( "TotalThreadsCpuTime;" + platformMonitoringAPI.getTotalThreadsCpuTime() + ";\n" );

        result = result.concat( "UpTime;" + platformMonitoringAPI.getUpTime() + ";\n" );

        result = result.concat( "IsSchedulerStarted ;" + platformMonitoringAPI.isSchedulerStarted() + ";\n" );

        String jvmSystemProp = "";

        for (Map.Entry<String, String> entry : platformMonitoringAPI.getJvmSystemProperties().entrySet()) {
            jvmSystemProp += entry.getValue();
        }

        result = result.concat( "CommitedVirtualMemorySize;" + jvmSystemProp + ";\n" );

        return result;
    }

    private static String getWebserverVersion() throws FileNotFoundException {

        String releaseNotesString = "";
        String webServer = "Tomcat";

        FileInputStream fis = new FileInputStream( catalinaBase+"/RELEASE-NOTES");
        Scanner sc = new Scanner( fis );    //file to be scanned

        //returns true if there is another line to read
        while (sc.hasNextLine() && !releaseNotesString.contains("Release Notes")) {
            releaseNotesString += sc.nextLine();      //returns the line that was skipped
        }
        sc.close();     //closes the scanner
        if(releaseNotesString!=null && !releaseNotesString.isEmpty()) {
            int indexStart = releaseNotesString.lastIndexOf( "Version " );
            int indexEnd = releaseNotesString.indexOf( " Release Notes" );

            webServer += "-" + releaseNotesString.substring( indexStart + 8, indexEnd );
        }

        return webServer;
    }
}
