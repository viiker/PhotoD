package com.viisoo.PhotoD;

import name.pachler.nio.file.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 目录监测
 * Created by dam on 2014/11/19.
 * 该源文件已被废弃
 */
public class NioPathWorker {

    protected Logger logger = Logger.getLogger(NoPathWorker.class);

    //设置只监测图片
    private PathMatcher pathMatcher = java.nio.file.FileSystems.getDefault().getPathMatcher("regex:([^\\s]+(\\.(?i)(png|PNG|jpg|JPG))$)");

    WatchService service = FileSystems.getDefault().newWatchService();

    private final Map<WatchKey, Path> directories = new ConcurrentHashMap<>();

    private Map<String,String> imgCache = new HashMap<String,String>();



    public void watchPath(String dirName){

        File imgPathFile = new File(dirName);
        if(!imgPathFile.exists()){
            logger.info("创建目录："+dirName);
            imgPathFile.mkdirs();
        }
        Path path = Paths.get(dirName);

        WatchKey key = null;
        try {
            key = path.register(service,
                    StandardWatchEventKind.ENTRY_CREATE,
                    StandardWatchEventKind.ENTRY_DELETE,
                    StandardWatchEventKind.ENTRY_MODIFY);
        } catch (UnsupportedOperationException uox){
            System.err.println("file watching is not supported!");
            // handle this error here
        } catch (IOException iox){
            System.err.println("I/O errors");
            // handle this error here
        }

        // typically, real world applications will run this loop in a
        // separate thread and signal directory changes to another thread
        // (often, this will be Swing's event dispatcher thread; use
        // SwingUtilities.invokeLater())
        for(;;){
            // take() will block until a file has been created/deleted
            WatchKey signalledKey = null;
            try {
                signalledKey = service.take();
            } catch (InterruptedException ix){
                // we'll ignore being interrupted
                continue;
            } catch (ClosedWatchServiceException cwse){
                // other thread closed watch service
                System.out.println("watch service is closed. terminating.");
                break;
            }

            // get list of events from key
            List<WatchEvent<?>> list = signalledKey.pollEvents();

            // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
            // key to be reported again by the watch service
            signalledKey.reset();

            // we'll simply print what has happened; real applications
            // will do something more sensible here
            for(WatchEvent e : list){
                String message = "";
                Path context = (Path)e.context();
                String filename = context.toString();
                if(e.kind() == StandardWatchEventKind.ENTRY_CREATE){
                    message = filename + " ----------created";
                    java.nio.file.Path filePath = java.nio.file.Paths.get(path.resolve(context).toString());

                    //System.out.println(path.resolve(context));
                } else if(e.kind() == StandardWatchEventKind.ENTRY_DELETE){
                    message = filename + " ----------deleted";
                } else if (e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
                    message = filename + " ----------modify";
                }else if(e.kind() == StandardWatchEventKind.OVERFLOW){
                    message = "OVERFLOW: more changes happened than we could retrieve.";
                }
                if (filename.endsWith("swp")||filename.endsWith("swpx"))
                {
                    continue;
                }else{
                    System.out.println(message);
                }
            }
        }
    }

    public static void main(String[] args) {
        NioPathWorker worker = new NioPathWorker();
        worker.watchPath("C:\\photoOutPath");
    }






}
