package com.bonitasoft.custompage.bigApp.setupconfiguration;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.*;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetupConfiguration {


    public static CollectResultDecoZip.ResultZip getSetupConfiguration() {

        String dir = "";

        if(System.getProperty("catalina.home")!=null && !System.getProperty("catalina.home").isEmpty()){
            dir = System.getProperty("catalina.home")+"/logs/";
        } else {
            dir = System.getProperty("jboss.server.log.dir")+"/";
        }

        //File fileBundle;

        //File fileBundle = null;
        File fileBundle = new File(dir);
        ArrayList<BEvent> listEvents = new ArrayList<BEvent>();

        try {
            fileBundle = new File(fileBundle.getCanonicalPath());
        } catch (Exception e) {
            listEvents.add( new BEvent(null, e, "ZIIIIP ["+e.getMessage()+"] END" ));
        }

        ConfigAPI.CollectParameter collectParameter = new ConfigAPI.CollectParameter();
        collectParameter.collectPlatformCharacteristic = true;
        collectParameter.collectServer = true;
        collectParameter.collectSetup = true;
        collectParameter.hidePassword = true;


        BonitaConfigPath localBonitaConfig = BonitaConfigPath.getInstance(fileBundle);
        ConfigAPI currentConfig = ConfigAPI.getInstance( localBonitaConfig );
        collectParameter.localFile = fileBundle;
        listEvents.addAll( currentConfig.setupPull() );

        // now, collect result
        CollectResult collectResult = currentConfig.collectParameters( collectParameter, CollectResult.COLLECTLOGSTRATEGY.LOGALL);

        // I want the result in JSON, so use a ResultDecoMap
        CollectResultDecoZip decoZip = new CollectResultDecoZip(collectResult);
        CollectResultDecoZip.ResultZip resultZip = decoZip.getZip( CollectOperation.TYPECOLLECT.SETUP );

        // collect errors
        listEvents.addAll( collectResult.getErrors());
        listEvents.addAll( resultZip.listEvents );

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> mapFile = new HashMap<String, Object>();
        mapFile.put("name", resultZip);
        result.add(mapFile);

        return resultZip;
    }

    /*public static void getSetupConfiguration() throws IOException {

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

    }*/
}
