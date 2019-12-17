package com.bonitasoft.custompage.bigApp.environment;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

import javax.servlet.http.HttpServlet;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentDetails extends HttpServlet {

    public static Map<String, Object> getEnvironment(APISession session) throws UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        Map<String, Object> result = new HashMap<String, Object>();

        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI( session );

        // Prepare the data that will be displayed on the page

        result.put( "operatingSystemInfos", platformMonitoringAPI.getOSName() + " - " + platformMonitoringAPI.getOSVersion() );

        result.put( "javaMachine", platformMonitoringAPI.getJvmName() + " " + platformMonitoringAPI.getJvmVersion() );

        result.put( "memoryUsage", platformMonitoringAPI.getMemoryUsagePercentage() + " % " );

        result.put( "availableProcessors", platformMonitoringAPI.getAvailableProcessors() );

        if (System.getProperty( "catalina.base" ) != null && System.getProperty( "catalina.base" ).contains( "tomcat" )) {
            result.put( "WebServer", "Tomcat" );
        } else if (System.getProperty( "jboss.server.config.dir" ) != null && System.getProperty( "jboss.server.config.dir" ).contains( "wildfly" )) {
            result.put( "WebServer", "Wildfly" );
        }

        return result;
    }

    public static String getEnvironmentInfosExport(APISession session) throws UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        String result = "";

        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI( session );

        // Prepare the data that will be available for download

        result = result.concat( "Parameter;value;\n" );

        result = result.concat( "operatingSystemInfos;" + platformMonitoringAPI.getOSName() + " - " + platformMonitoringAPI.getOSVersion() + ";\n" );

        if (System.getProperty( "catalina.base" ) != null && System.getProperty( "catalina.base" ).contains( "tomcat" )) {
            result = result.concat( "WebServer;" + "Tomcat" + ";\n" );
        } else if (System.getProperty( "jboss.server.config.dir" ) != null && System.getProperty( "jboss.server.config.dir" ).contains( "wildfly" )) {
            result = result.concat( "WebServer;" + "Wildfly" + ";\n" );
        }

        result = result.concat( "javaMachine;" + platformMonitoringAPI.getJvmName() + " " + platformMonitoringAPI.getJvmVersion() + ";\n" );

        result = result.concat( "memoryUsage;" + platformMonitoringAPI.getMemoryUsagePercentage() + " % " + ";\n" );

        result = result.concat( "availableProcessors;" + platformMonitoringAPI.getAvailableProcessors() + ";\n" );

        result = result.concat( "MemUsage;" + platformMonitoringAPI.getCurrentMemoryUsage() / 1024 + ";\n" );

        result = result.concat( "MemFree;" + platformMonitoringAPI.getFreePhysicalMemorySize() / 1024 + ";\n" );

        result = result.concat( "MemFreeSwap;" + platformMonitoringAPI.getFreeSwapSpaceSize() + ";\n" );

        result = result.concat( "MemTotalPhysicalMemory;" + platformMonitoringAPI.getTotalPhysicalMemorySize() + ";\n" );

        result = result.concat( "MemTotalSwapSpace;" + platformMonitoringAPI.getTotalSwapSpaceSize() + ";\n" );

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

        result = result.concat( "ProcessCPUTime;" + platformMonitoringAPI.getProcessCpuTime() + ";\n" );

        result = result.concat( "StartTime;" + platformMonitoringAPI.getStartTime() + ";\n" );

        result = result.concat( "LoadAverageLastMn;" + platformMonitoringAPI.getSystemLoadAverage() + ";\n" );

        result = result.concat( "ThreadCount;" + platformMonitoringAPI.getThreadCount() + ";\n" );

        result = result.concat( "TotalThreadsCpuTime;" + platformMonitoringAPI.getTotalThreadsCpuTime() + ";\n" );

        result = result.concat( "UpTime;" + platformMonitoringAPI.getUpTime() + ";\n" );

        result = result.concat( "IsSchedulerStarted ;" + platformMonitoringAPI.isSchedulerStarted() + ";\n" );

        result = result.concat( "CommitedVirtualMemorySize;" + platformMonitoringAPI.getCommittedVirtualMemorySize() + ";\n" );

        String jvmSystemProp = "";

        for (Map.Entry<String, String> entry : platformMonitoringAPI.getJvmSystemProperties().entrySet()) {
            jvmSystemProp += entry.getValue();
        }

        result = result.concat( "CommitedVirtualMemorySize;" + jvmSystemProp + ";\n" );

        return result;
    }
}
