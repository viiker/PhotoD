package com.viisoo.PhotoD;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by niweihua on 2017/10/21.
 */
public class FileUtility {
    public FileUtility() {

    }

    public static List<String> listDirectoryFiles(String dirName){
        final List<String> fileNameList = new ArrayList<String>();
        try {
            Files.walkFileTree(Paths.get(dirName), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fileNameList.add(file.getFileName().toString());
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNameList;
    }

    public static void deleteFile(String fileUri) {
        File file = new File(fileUri);
        if(file.exists()) {
            file.delete();
        }

    }

    public static String getFileModifyDate(String fileUri) {
        FileTime fileTime = null;
        Path path = Paths.get(fileUri, new String[0]);

        try {
            fileTime = Files.getLastModifiedTime(path, new LinkOption[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date(fileTime.toMillis()));
    }
}
