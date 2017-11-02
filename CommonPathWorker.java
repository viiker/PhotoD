package com.viisoo.PhotoD;


import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * set the commom path
 */
public class CommonPathWorker {

    protected Logger logger = Logger.getLogger(CommonPathWorker.class);

    private static BlockingQueue<File> imgQueue = new ArrayBlockingQueue<>(20);

    IOFileFilter dirs = FileFilterUtils.and(
            FileFilterUtils.directoryFileFilter(),
            HiddenFileFilter.VISIBLE);

    IOFileFilter smallJpg = FileFilterUtils.and(
            FileFilterUtils.fileFileFilter(),
            FileFilterUtils.suffixFileFilter(".jpg"),
            FileFilterUtils.notFileFilter(new WildcardFileFilter("*_small_*")));

    IOFileFilter smallJPG = FileFilterUtils.and(
            FileFilterUtils.fileFileFilter(),
            FileFilterUtils.suffixFileFilter(".JPG"),
            FileFilterUtils.notFileFilter(new WildcardFileFilter("*_small_*")));

    IOFileFilter filter = FileFilterUtils.or(dirs, smallJpg, smallJPG);

    private static MyProperties property;
    private static String imageInPath;
    private static String imageOutPath;
    private static String onlineMode;
    private static String qnUserName;
    private static String qnDomain;
    private static double xShiftD;
    private static double yShiftD;
    private static int photoSize;

    public CommonPathWorker() {
        property = new MyProperties();
        imageInPath = property.getPropertiesValue("PHOTO_IN_PATH");
        imageOutPath = property.getPropertiesValue("PHOTO_OUT_PATH");
        onlineMode = property.getPropertiesValue("IS_ONLINE");
        qnUserName = property.getPropertiesValue("USERNAME");
        qnDomain = property.getPropertiesValue("DOMAIN");
        String xShift = property.getPropertiesValue("X_SHIFT");
        String yShift = property.getPropertiesValue("Y_SHIFT");
        xShiftD = StringUtils.isNotBlank(xShift) ? Float.parseFloat(xShift) : Constants.X_SHIFT;
        yShiftD = StringUtils.isNotBlank(yShift) ? Float.parseFloat(yShift) : Constants.Y_SHIFT;
        String photoSizeStr = property.getPropertiesValue("PHOTO_SIZE");
        photoSize = StringUtils.isNotBlank(photoSizeStr) ? Integer.parseInt(photoSizeStr) : Constants.PHOTO_SIZE;
	}

	public void handleRestImage() {
        final List<String> tdFileList = FileUtility.listDirectoryFiles(imageOutPath);
        String[] extensions = new String[]{"jpg", "JPG"};
        List<File> files = (List<File>) org.apache.commons.io.FileUtils.listFiles(new File(imageInPath), extensions, true);

        int i = 0;
        for (File file : files) {
            String imageName = file.getName();
            String relativeName = file.toString().substring(imageInPath.length() + 1);
            if (StringUtils.isNotBlank(onlineMode) && onlineMode.equals("true")) {
                // 在线模式处理剩余照片，仅上传
                // 后续需要考虑上传的条件，如果文件已经存在于服务器，则不上传了
                if(imageName.indexOf("_small_") == -1) {
                    i++;
                    if(1 == i) {
                        this.logger.info("上传既存的照片：");
                    }
                    this.logger.info(i + ". " + relativeName);
                    ImageUpload.call(file, imageInPath, qnUserName, photoSize);
                    this.logger.info("照片已上传成功。");
                }
            } else {
                // 离线模式处理剩余照片，仅生成二维码
                if (imageName.indexOf("_small_") == -1 && !tdFileList.contains(imageName)) {
                    i++;
                    if(1 == i) {
                        this.logger.info("处理剩余的照片：");
                    }
                    this.logger.info(i + ". " + relativeName);
                    ImageCompose.call(file, imageInPath, imageOutPath, qnDomain, xShiftD, yShiftD);
                    this.logger.info("照片已生成二维码，请至照片打印目录查看。");
                }
            }
        }
    }

    public void monitorDirectory() {
        this.logger.info("\n监视照片回收目录：");

        for(int i = 0;i < 10; i ++){
            Consumer consumer = new Consumer();
            consumer.start();
        }
        // 轮询间隔 3 秒
        long interval = TimeUnit.SECONDS.toMillis(3L);

        // 创建一个文件观察器用于处理文件的格式
        FileAlterationObserver observer = new FileAlterationObserver(imageInPath, filter, (IOCase)null);
        // 设置文件变化监听器
        observer.addListener(new FileMonitorFileListener());
        // 创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, new FileAlterationObserver[]{observer});
        // 开始监视
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class FileMonitorFileListener extends FileAlterationListenerAdaptor {
        protected Logger logger = Logger.getLogger(FileMonitorFileListener.class);
        /**
         * 文件创建执行
         */
        @Override
        public void onFileCreate(File file) {
            String relativeName = file.toString().substring(imageInPath.length() + 1);
            this.logger.info("[发现]: " + relativeName);
            try {
                imgQueue.put(file);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 文件创建修改
         */
        @Override
        public void onFileChange(File file) {
            String relativeName = file.toString().substring(imageInPath.length() + 1);
            this.logger.info("[修改]: " + relativeName);
        }

        /**
         * 文件删除
         */
        @Override
        public void onFileDelete(File file) {
            String relativeName = file.toString().substring(imageInPath.length() + 1);
            this.logger.info("[删除]: " + relativeName);
        }
    }

    class Consumer extends Thread{
        protected Logger logger = Logger.getLogger(Consumer.class);
        public void run(){
            consume();
        }
        private void consume() {
            while(true){
                try {
                    File file = imgQueue.take();
                    String relativeName = file.toString().substring(imageInPath.length() + 1);
                    if (StringUtils.isNotBlank(onlineMode) && onlineMode.equals("true")) {
                        // 在线模式处理实时接收的照片，仅上传
                        // 后续需要增加上传条件判断，已经上传过的，将不再上传
                        this.logger.info("[上传]：" + relativeName);
                        ImageUpload.call(file, imageInPath, qnUserName, photoSize);
                    } else {
                        // 离线模式处理实时接收的照片，仅生成二维码
                        this.logger.info("[处理]：" + relativeName);
                        ImageCompose.call(file, imageInPath, imageOutPath, qnDomain, xShiftD, yShiftD);
                    }
                    this.logger.info("[完成]：" + relativeName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
