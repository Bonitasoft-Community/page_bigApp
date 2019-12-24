package com.bonitasoft.custompage.bigApp.logs;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Logs {

    public static List<Map<String, Object>> getLogs() throws IOException {

        String dir = "";

        if (System.getProperty( "catalina.home" ) != null && !System.getProperty( "catalina.home" ).isEmpty()) {
            dir = System.getProperty( "catalina.home" ) + "/logs/";
        } else {
            dir = System.getProperty( "jboss.server.log.dir" ) + "/";
        }

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        File directory = new File( dir );
        File[] filesArray = directory.listFiles();

        Arrays.sort( filesArray, Comparator.comparingLong( File::lastModified ).reversed() );

        //print the sorted values
        for (File file : filesArray) {
            Map<String, Object> mapFile = new HashMap<String, Object>();
            result.add( mapFile );
            mapFile.put( "name", file.getName() );
        }

        return result;
    }

}
