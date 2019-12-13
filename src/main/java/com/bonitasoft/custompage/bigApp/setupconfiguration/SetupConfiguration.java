package com.bonitasoft.custompage.bigApp.setupconfiguration;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.*;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult.COLLECTLOGSTRATEGY;
import org.bonitasoft.serverconfiguration.CollectResultDecoZip.ResultZip;
import org.bonitasoft.serverconfiguration.ConfigAPI.CollectParameter;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipOutputStream;

public class SetupConfiguration {


    public static CollectResultDecoZip.ResultZip getSetupConfiguration(File pageDirectory) {
        ResultZip finalResultZip = new ResultZip();  

        String dir = "";

        if(System.getProperty("catalina.home")!=null && !System.getProperty("catalina.home").isEmpty()){
            dir = System.getProperty("catalina.home")+"/logs/";
        } else {
            dir = System.getProperty("jboss.server.log.dir")+"/";
        }

        File fileBundle = null;
        fileBundle = new File(pageDirectory.getAbsoluteFile() + "/../../../../../../../");
      
              
        try {
            fileBundle = new File(fileBundle.getCanonicalPath());
        } catch (Exception e) {
            finalResultZip.listEvents.add( new BEvent(null, e, "ZIIIIP ["+e.getMessage()+"] END" ));
        }

        ConfigAPI.CollectParameter collectParameter = new ConfigAPI.CollectParameter();
        collectParameter.listTypeCollect.add( TYPECOLLECT.PLATFORM);
        collectParameter.listTypeCollect.add( TYPECOLLECT.SERVER);
        collectParameter.listTypeCollect.add( TYPECOLLECT.SETUP);

        collectParameter.hidePassword = true;

        BonitaConfigPath localBonitaConfig = BonitaConfigPath.getInstance(fileBundle);
        ConfigAPI currentConfig = ConfigAPI.getInstance( localBonitaConfig );
        collectParameter.localFile = fileBundle;
        finalResultZip.listEvents.addAll( currentConfig.setupPull() );

     
        
        // now, collect result
        CollectResult collectResult = currentConfig.collectParameters( collectParameter, CollectResult.COLLECTLOGSTRATEGY.LOGALL);
        finalResultZip.listEvents.addAll( collectResult.getErrors());

        // I want the result in JSON, so use a ResultDecoMap
        CollectResultDecoZip decoZip = new CollectResultDecoZip(collectResult);
        List<TYPECOLLECT> listToCollect = Arrays.asList( CollectOperation.TYPECOLLECT.SETUP, CollectOperation.TYPECOLLECT.SERVER);
        
        finalResultZip.zipContent = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( finalResultZip.zipContent );
       
        // todo : add any file you want in the zip
        
        
        CollectResultDecoZip.ResultZip resultZipDeco = decoZip.addToZip( listToCollect, zos );
        finalResultZip.listEvents.addAll( resultZipDeco.listEvents );

        try {
            zos.close();
        } catch (IOException e) {
        }
        
        return finalResultZip;
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
