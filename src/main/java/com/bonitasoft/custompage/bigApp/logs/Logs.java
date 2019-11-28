package com.bonitasoft.custompage.bigApp.logs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Logs {

    public static Map<String, Object> getLogs() throws IOException {

        String dir = System.getProperty("catalina.home");

        Set<String> fileList = new HashSet<>();
        Map<String, Object> result = new HashMap<String, Object>();
        String i = "";
        Integer j = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir+"/logs/"))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(path.getFileName().toString());
                    result.put( String.valueOf(j), path.getFileName().toString());
                    j++;
                }
            }
        }

        return result;
    }
}
