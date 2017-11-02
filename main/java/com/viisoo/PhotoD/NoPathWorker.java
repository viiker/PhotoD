package com.viisoo.PhotoD;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 目录监控
 * Created by dam on 2014/11/19.
 * 该源文件已被废弃
 */
public class NoPathWorker {

    protected Logger logger = Logger.getLogger(NoPathWorker.class);

    //设置只监控图片
    private PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("regex:([^\\s]+(\\.(?i)(png|PNG|jpg|JPG))$)");

    private WatchService service = null;

    private final Map<WatchKey, Path> directories = new ConcurrentHashMap<>();

    //private BlockingQueue<String> imgQueue = new ArrayBlockingQueue<String>(10);
    private Map<String,String> imgCache = new HashMap<String,String>();

    public NoPathWorker(){
        try {
            service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void watchPath(String dirName){

        File imgPathFile = new File(dirName);
        if(!imgPathFile.exists()){
            logger.info("创建目录："+dirName);
            imgPathFile.mkdirs();
        }

        try {

            Files.walkFileTree(Paths.get(dirName),new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(!dir.getFileName().toString().equals("td")){
                        logger.info("监视目录："+dir);
                        WatchKey key = dir.register(service,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.OVERFLOW);
                        directories.put(key,dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 监控某种新增的文件
     * @return
     */
    public void findUploadImg(){
        try {

            while (true){

                WatchKey key = service.take();
                for(WatchEvent<?> event : key.pollEvents()){
                    final WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path)event.context();
                    Path imgDir = directories.get(key);
                    Path imgPath = imgDir.resolve(fileName);

                    if(kind == StandardWatchEventKinds.OVERFLOW){
                        continue;
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        final Path directory_path = directories.get(key);
                        final Path child = directory_path.resolve(fileName);

                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            watchPath(child.toString());
                            //uploadOldImg(child.toString());
                        }


                    }else if(kind == StandardWatchEventKinds.ENTRY_MODIFY){



                    }
                    if(pathMatcher.matches(fileName.getFileName())
                            && fileName.getFileName().toString().indexOf("_small") == -1){

                        //System.out.println(imgPath.toString()+" "+kind+" "+imgCache.get(imgPath.toString())+" "+Files.exists(imgPath));
                        if(Files.exists(imgPath)){

                            if(imgCache.get(imgPath.toString()) == null){
                                System.out.println(imgPath.toString()+" upload");
                                imgCache.put(imgPath.toString(), imgPath.toString());
                                //UploadImgTask.addToUploadPool(imgPath.toString());
                            }

                        }

                    }

                }
                key.reset();
            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }

    }


//    public void uploadOldImg(String imgPath){
//
//        String newFilePath = FileUtils.getPropertiesValue(FileUtils.findJarPath(),"PHOTO_OUT_PATH");
//        final List<String> tdFileList =  FileUtils.listDirectoryFiles(newFilePath);
//        try {
//            Files.walkFileTree(Paths.get(imgPath), new SimpleFileVisitor<Path>(){
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    String parentDir = file.getParent().getFileName().toString();
//                    if(!parentDir.equals("td")){
//                        String imageName = file.getFileName().toString();
//                        if(pathMatcher.matches(file.getFileName())){
//                            if(!tdFileList.contains(imageName)){
//                                logger.info("上传之前遗留的照片"+file.toString());
//                                UploadImgTask.addToUploadPool(file.toString());
//                            }
//                        }
//                    }
//
//                    return super.visitFile(file, attrs);
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }



}
