package com.bonitasoft.custompage.bigApp.environment;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import com.bonitasoft.engine.monitoring.MonitoringException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.session.APISession;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentDetails {
    public static Map<String, Object> getEnvironment(APISession session) throws BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {


        Map<String, Object> result = new HashMap<String, Object>();
        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI(session);

        // Prepare the data that will be displayed on the page

        result.put("operatingSystemInfos", platformMonitoringAPI.getOSName() + " - " + platformMonitoringAPI.getOSVersion());

        result.put("javaMachine", platformMonitoringAPI.getJvmName() + " " + platformMonitoringAPI.getJvmVersion());

        result.put("memoryUsage", platformMonitoringAPI.getMemoryUsagePercentage()+ " % ");

        result.put("availableProcessors", platformMonitoringAPI.getAvailableProcessors());

        // Prepare the data that will be available for download

        result.put("MemUsage",platformMonitoringAPI.getCurrentMemoryUsage() / 1024);

        result.put("MemFree",platformMonitoringAPI.getFreePhysicalMemorySize() / 1024);

        result.put("MemFreeSwap", platformMonitoringAPI.getFreeSwapSpaceSize());

        result.put("MemTotalPhysicalMemory", platformMonitoringAPI.getTotalPhysicalMemorySize());

        result.put("MemTotalSwapSpace", platformMonitoringAPI.getTotalSwapSpaceSize());

        result.put("JavaFreeMemory",Runtime.getRuntime().freeMemory() / 1024 / 1024);

        result.put("JavaTotalMemory",Runtime.getRuntime().totalMemory() / 1024 / 1024);

        result.put("JavaUsedMemory",(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);

        result.put("JvmVendor", platformMonitoringAPI.getJvmVendor());

        result.put("JvmVersion", platformMonitoringAPI.getJvmVersion());

        result.put("MemUsagePercentage",platformMonitoringAPI.getMemoryUsagePercentage());

        result.put("NumberActiveTransaction", platformMonitoringAPI.getNumberOfActiveTransactions());

        result.put("OSArch", platformMonitoringAPI.getOSArch());

        result.put("OSName", platformMonitoringAPI.getOSName());

        result.put("OSVersion", platformMonitoringAPI.getOSVersion());

        result.put("ProcessCPUTime", platformMonitoringAPI.getProcessCpuTime());

        result.put("StartTime", platformMonitoringAPI.getStartTime());

        result.put("LoadAverageLastMn", platformMonitoringAPI.getSystemLoadAverage());

        result.put("ThreadCount", platformMonitoringAPI.getThreadCount());

        result.put("TotalThreadsCpuTime", platformMonitoringAPI.getTotalThreadsCpuTime());

        result.put("UpTime", platformMonitoringAPI.getUpTime());

        result.put("IsSchedulerStarted ", platformMonitoringAPI.isSchedulerStarted());

        result.put("CommitedVirtualMemorySize", platformMonitoringAPI.getCommittedVirtualMemorySize());

        String jvmSystemProp = "";

        for (Map.Entry<String, String> entry : platformMonitoringAPI.getJvmSystemProperties().entrySet()) {
            jvmSystemProp += entry.getValue();
        }

        result.put("CommitedVirtualMemorySize", jvmSystemProp);

        return result;
    }
}
