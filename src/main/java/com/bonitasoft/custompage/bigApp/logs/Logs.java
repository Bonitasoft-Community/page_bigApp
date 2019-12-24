package com.bonitasoft.custompage.bigApp.logs;

import java.io.File;
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

        /*Set<String> fileList = new HashSet<>();
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
        }*/

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        File directory = new File(dir);
        File[] filesArray = directory.listFiles();
        //sort all files
        /*Arrays.sort(filesArray, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        }.reversed());*/
        Comparator.comparingLong(File::lastModified).reversed();
        Arrays.sort(filesArray, Collections.reverseOrder());
        //print the sorted values
        for (File file : filesArray) {
            if(file.getName().contains( "bonita" )) {
                Map<String, Object> mapFile = new HashMap<String, Object>();
                result.add( mapFile );
                mapFile.put( "name", file.getName() );
            }
        }
        for (File file : filesArray) {
            if(!file.getName().contains( "bonita" )) {
                Map<String, Object> mapFile = new HashMap<String, Object>();
                result.add( mapFile );
                mapFile.put( "name", file.getName() );
            }
        }

        return result;
    }

}
