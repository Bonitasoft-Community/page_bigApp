package com.bonitasoft.custompage.bigApp.logs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Logs {

    public static List<Map<String, Object>> getLogs() throws IOException {

        String dir = "";

        if (System.getProperty( "catalina.home" ) != null && !System.getProperty( "catalina.home" ).isEmpty()) {
            dir = System.getProperty( "catalina.home" ) + "/logs/";
        } else {
            dir = System.getProperty( "jboss.server.log.dir" ) + "/";
        }

        Set<String> fileList = new HashSet<>();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Integer j = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get( dir ) )) {
            for (Path path : stream) {
                if (!Files.isDirectory( path )) {
                    fileList.add( path.getFileName().toString() );
                    Map<String, Object> mapFile = new HashMap<String, Object>();
                    result.add( mapFile );
                    mapFile.put( "name", path.getFileName().toString() );
                }
            }
        }

        return result;
    }

}
