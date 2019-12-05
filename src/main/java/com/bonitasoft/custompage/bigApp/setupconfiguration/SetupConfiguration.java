package com.bonitasoft.custompage.bigApp.setupconfiguration;

import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.CollectResultDecoMap;
import org.bonitasoft.serverconfiguration.ConfigAPI;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SetupConfiguration {

    public static void getSetupConfiguration() throws IOException {

        String dir = System.getProperty("catalina.home");
        File fileBundle = new File(dir + "/../../../../../../../");

        fileBundle = new File(fileBundle.getCanonicalPath());

        Map<String, Object> parameters = null;
        CollectParameter collectParameter = CollectParameter.getInstanceFromMap(parameters);
        BonitaConfigPath localBonitaConfig;
        ConfigAPI currentConfig;

        localBonitaConfig = BonitaConfigPath.getInstance(fileBundle);
        currentConfig = ConfigAPI.getInstance(localBonitaConfig);
        collectParameter.localFile = fileBundle;
        currentConfig.setupPull();

        // now, collect result
        CollectResult collectResult = currentConfig.collectParameters(collectParameter, COLLECTLOGSTRATEGY.LOGALL);

        // I want the result in JSON, so use a ResultDecoMap
        CollectResultDecoMap decoMap = new CollectResultDecoMap(collectResult, "", localBonitaConfig.getRootPath() );

        // ok, get the value of decoration
        Map<String, Object> result = decoMap.getMap();

    }
}
