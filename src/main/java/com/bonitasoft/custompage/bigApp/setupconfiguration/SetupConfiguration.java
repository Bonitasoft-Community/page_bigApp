package com.bonitasoft.custompage.bigApp.setupconfiguration;

import com.bonitasoft.custompage.bigApp.environment.EnvironmentDetails;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.serverconfiguration.CollectOperation;
import org.bonitasoft.serverconfiguration.CollectOperation.TYPECOLLECT;
import org.bonitasoft.serverconfiguration.CollectResult;
import org.bonitasoft.serverconfiguration.CollectResultDecoZip;
import org.bonitasoft.serverconfiguration.CollectResultDecoZip.ResultZip;
import org.bonitasoft.serverconfiguration.ConfigAPI;
import org.bonitasoft.serverconfiguration.referentiel.BonitaConfigPath;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SetupConfiguration {

    public static BEvent EVENT_ZIP_ENTRY;
    public static BEvent EVENT_ZIP_CLOSE_ENTRY;

    static {
        EVENT_ZIP_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 1L, BEvent.Level.ERROR, "Zip Error", "An severe error when zipping a file", "This file won't be in the final zip", "Check this file" );
        EVENT_ZIP_CLOSE_ENTRY = new BEvent( CollectResultDecoZip.class.getName(), 2L, BEvent.Level.ERROR, "Close Zip Entry Error", "An severe error during zipping a file", "This file is probably corrupt", "Check the error" );
    }


    public static CollectResultDecoZip.ResultZip getSetupConfiguration(File pageDirectory, APISession session, String listLogs, Boolean pullConfActivated) throws java.lang.reflect.InvocationTargetException, java.lang.IllegalAccessException, NoSuchMethodException, IOException, UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        ResultZip finalResultZip = new ResultZip();

        File fileBundle = null;
        Boolean serverTomcat = false;

        if (System.getProperty( "catalina.home" ) != null && !System.getProperty( "catalina.home" ).isEmpty()) {
            fileBundle = new File( pageDirectory.getAbsoluteFile() + "/../../../../../../../" );
            serverTomcat = true;
        } else {
            fileBundle = new File( System.getProperty( "jboss.home.dir" ) ).getParentFile();
        }

        try {
            fileBundle = new File( fileBundle.getCanonicalPath() );
        } catch (Exception e) {
            finalResultZip.listEvents.add( new BEvent( null, e, "ZIIIIP [" + e.getMessage() + "] END" ) );
        }

        ConfigAPI.CollectParameter collectParameter = new ConfigAPI.CollectParameter();
        collectParameter.listTypeCollect.add( TYPECOLLECT.PLATFORM );
        collectParameter.listTypeCollect.add( TYPECOLLECT.SERVER );
        collectParameter.listTypeCollect.add( TYPECOLLECT.SETUP );

        collectParameter.hidePassword = true;

        BonitaConfigPath localBonitaConfig = BonitaConfigPath.getInstance( fileBundle );
        ConfigAPI currentConfig = ConfigAPI.getInstance( localBonitaConfig );
        collectParameter.localFile = fileBundle;
        finalResultZip.listEvents.addAll( currentConfig.setupPull() );


        // now, collect result
        CollectResult collectResult = currentConfig.collectParameters( collectParameter, CollectResult.COLLECTLOGSTRATEGY.LOGALL, null );
        finalResultZip.listEvents.addAll( collectResult.getErrors() );

        // I want the result in JSON, so use a ResultDecoMap
        CollectResultDecoZip decoZip = new CollectResultDecoZip( collectResult );
        List<TYPECOLLECT> listToCollect = Arrays.asList( CollectOperation.TYPECOLLECT.SETUP, CollectOperation.TYPECOLLECT.SERVER );

        finalResultZip.zipContent = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream( finalResultZip.zipContent );

        // Create the files that we want to add into the result file zip
        List<File> filesList = new ArrayList<File>();
        File setupFile = new File( localBonitaConfig.getRootPath() + "/setup" );
        File serverConfFile = new File( localBonitaConfig.getRootPath() + "/server/conf" );
        File serverBinFile = new File( localBonitaConfig.getRootPath() + "/server/bin" );
        File serverStandaloneConfFile = new File( localBonitaConfig.getRootPath() + "/server/standalone/configuration" );
        File serverModulesOrgFile = new File( localBonitaConfig.getRootPath() + "/server/modules/org" );
        File serverModulesComFile = new File( localBonitaConfig.getRootPath() + "/server/modules/com" );

        // Create a list with the files we want in the final zip file + The parent directory they're located at
        Map<File, String> filesParentsDirectoryMap = new HashMap<>();
        filesParentsDirectoryMap.put( setupFile, "/setup" );
        if (serverTomcat) {
            filesParentsDirectoryMap.put( serverConfFile, "/server/conf" );
            filesParentsDirectoryMap.put( serverBinFile, "/server/bin" );
        } else {
            filesParentsDirectoryMap.put( serverStandaloneConfFile, "/server/standalone/configuration" );
            if (null != serverModulesOrgFile && 0 != serverModulesOrgFile.length()) {
                filesParentsDirectoryMap.put( serverModulesOrgFile, "/server/modules/org" );
            }
            if (null != serverModulesComFile && 0 != serverModulesComFile.length()) {
                filesParentsDirectoryMap.put( serverModulesComFile, "/server/modules/com" );
            }
        }

        // Adds Environmental information
        addEnvironmentDetails( zos, session, setupFile );

        // Adds Log files selected by the user
        addLogFiles( zos, session, listLogs );

        // When the user checks the "add setupConfiguration" option we call the method that will zip all the files that we have specified in filesParentsDirectoryMap
        if (pullConfActivated) {
            ResultZip resultZip = new ResultZip();
            filesParentsDirectoryMap.forEach( (fileToZip, parentDirectoryName) -> {
                for (File file : fileToZip.listFiles()) {
                    CollectResultDecoZip.ResultZip resultZipDeco = decoZip.addDirectoriesToZip( zos, file, resultZip, parentDirectoryName );
                    finalResultZip.listEvents.addAll( resultZipDeco.listEvents );
                }
            } );
        }
        try {
            zos.close();
        } catch (IOException e) {
        }

        return finalResultZip;
    }

    private static void addEnvironmentDetails(ZipOutputStream zos, APISession session, File setupFile) throws java.lang.reflect.InvocationTargetException, java.lang.IllegalAccessException, NoSuchMethodException, IOException, UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        File environmentDetailsCSV = null;
        String content = EnvironmentDetails.getEnvironmentInfosExport( session, setupFile );
        environmentDetailsCSV = new File( "EnvironmentalInformation.csv" );
        environmentDetailsCSV.createNewFile();
        addFileToZip( zos, environmentDetailsCSV, content );

    }

    private static void addLogFiles(ZipOutputStream zos, APISession session, String strListLogs) throws IOException, UnknownHostException, BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException, MonitoringException, UnavailableInformationException {

        File logFile = null;
        File oldLogFile = null;
        String listLogs[] = strListLogs.split( "," );

        String dir = "";

        if (System.getProperty( "catalina.home" ) != null && !System.getProperty( "catalina.home" ).isEmpty()) {
            dir = System.getProperty( "catalina.home" ) + "/logs/";
        } else {
            dir = System.getProperty( "jboss.server.log.dir" ) + "/";
        }

        for (int i = 0; i < listLogs.length; i++) {
            oldLogFile = new File( dir + listLogs[i] );
            addFileToZip( zos, oldLogFile, null );
        }
    }

    private static void addFileToZip(ZipOutputStream zos, File fileToZip, String content) {

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte[] bytes = new byte[2048];

        if (fileToZip == null) {
            //resultZip.listEvents.add(new BEvent(EVENT_ZIP_ENTRY, "fileName [no file]"));
        } else {
            try {
                fis = new FileInputStream( fileToZip );
                bis = new BufferedInputStream( fis );
                zos.putNextEntry( new ZipEntry( fileToZip.getName() ) );

                int bytesRead;
                if (content != null) {
                    zos.write( content.getBytes(), 0, content.length() );

                } else {
                    while ((bytesRead = bis.read( bytes )) != -1) {
                        zos.write( bytes, 0, bytesRead );
                    }
                }
            } catch (Exception var9) {
                //resultZip.listEvents.add(new BEvent(EVENT_ZIP_ENTRY, var9, "fileName [" + fileToZip.getName() + "]"));
            }

            try {
                if (zos != null) {
                    zos.closeEntry();
                }

                if (bis != null) {
                    bis.close();
                }

                if (fis != null) {
                    fis.close();
                }
            } catch (Exception var8) {
                //resultZip.listEvents.add(new BEvent(EVENT_ZIP_CLOSE_ENTRY, var8, "fileName [" + fileToZip.getName() + "]"));
            }

        }
    }

}
