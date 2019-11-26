package com.bonitasoft.custompage.bigApp.environment;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import com.bonitasoft.engine.monitoring.MonitoringException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.session.APISession;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentDetails {
    public static Map<String, Object> getEnvironment(APISession session) throws BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException {


        Map<String, Object> result = new HashMap<String, Object>();
        PlatformMonitoringAPI platformMonitoringAPI = TenantAPIAccessor.getPlatformMonitoringAPI(session);

        // Prepare the data that will be displayed on the page
        result.put("operatingSystemInfos", TenantAPIAccessor.getPlatformMonitoringAPI(session).getOSName() + " ******** " + TenantAPIAccessor.getPlatformMonitoringAPI(session).getOSVersion());
        result.put("JavaMachine", TenantAPIAccessor.getPlatformMonitoringAPI(session).getJvmVersion());
        result.put("availableProcessors", TenantAPIAccessor.getPlatformMonitoringAPI(session).getAvailableProcessors());

        return result;
    }
}
