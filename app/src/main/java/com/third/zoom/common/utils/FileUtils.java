package com.third.zoom.common.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * 作者：Sky on 2018/9/4.
 * 用途：文件管理、操作
 */

public class FileUtils {

    private final static int COPY_FAIL = -1;
    private final static int COPY_SUCCESS = 0;
    //private final static int COPY_START = 1;
    private final static String FROMPATH_MEDIA_UDISK = "/storage/udisk3/VIDEO";
    private final static String FROMPATH_MUSIC_UDISK = "/storage/udisk3/MUSIC";
    private final static String FROMPATH_PHOTO_UDISK = "/storage/udisk3/PHOTO";
    private final static String TOPATH_MEDIA_UDISK = "/storage/card/VIDEO/";
    private final static String TOPATH_MUSIC_UDISK = "/storage/card/MUSIC/";
    private final static String TOPATH_PHOTO_UDISK = "/storage/card/PHOTO/";

    Copyer copyer;
    public FileUtils(){
        copyer = new Copyer();
    }
    /**
     * 保存错误记录
     * @param msg
     */
    public static void saveFileForError(String msg) {

        String basePath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/YTBus";
        File base = new File(basePath);
        if(base == null || !base.exists()){
            base.mkdirs();
        }

        String driverPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/YTBus/operation.log";
        File driverFile = new File(driverPath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(driverFile);
            out.write(msg.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void startCopy(final String fromPath, final String toPath) {
        //mHandler.sendEmptyMessage(COPY_START);
        //mCopyStartBtn.setClickable(false);
//        new Thread() {
//            public void run() {
                if (copy(fromPath, toPath) == COPY_SUCCESS) {
                    //mHandler.sendEmptyMessage(COPY_SUCCESS);
                    Log.i("zhu_test","copy success...");
                } else {
                    //mHandler.sendEmptyMessage(COPY_FAIL);
                }
            //}
//        }.start();
    }

    /**
     * 拷贝函数
     * @param fromFile 文件来源路径
     * @param toFile 拷贝到的路径
     * @return
     */
    public int copy(String fromFile, String toFile) {
        long start=System.currentTimeMillis();
        File[] currentFiles;
        File root = new File(fromFile);
        if (!root.exists()) {
            Log.i("zhu_test","U disk files no exists...");
            return COPY_FAIL;
        }
        currentFiles = root.listFiles();
        File targetDir = new File(toFile);

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (int i = 0; i < currentFiles.length; i++) {
            Log.i("zhu_test","currentFiles[i].isDirectory()=="+currentFiles[i].isDirectory());
            if (currentFiles[i].isDirectory())
            {
                copy(currentFiles[i].getPath() + "/",
                        toFile + currentFiles[i].getName() + "/");
            }
            else
            {
                copyFile(currentFiles[i].getPath(), toFile
                        + currentFiles[i].getName());
                //copyer.copy(currentFiles[i],toFile,10);

                //copyFile1(currentFiles[i].getPath(), toFile
                //        + currentFiles[i].getName());
                //Log.i("zhu_test","copy file spend time=="+(System.currentTimeMillis()-start));
            }
        }
        return COPY_SUCCESS;
    }
    public void copyFile1(String fromFile, String toFile){
        long filelength = new File(fromFile).length();
        long size = filelength/5;
//        Date date = new Date();
//        Log.i("zhu_test","文件拷贝开始时间为："+date+"\n 正在拷贝文件······");
        //定义5个线程
        for (int i = 0; i < 5; i++) {
            new ThreadCopyFile(fromFile,toFile,i*size,(i+1)*size).start();
        }
//		Thr
    }
    /**
     * 拷贝单一文件
     * @param fromFile 文件来源路径
     * @param toFile 拷贝到的路径
     * @return
     */
    public int copyFile(String fromFile, String toFile) {
        try {
            Log.i("zhu_test","copyFile fromFile=="+fromFile+",toFile=="+toFile);
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
//            currentSize += (new File(fromFile)).length();
//            mHandler.sendEmptyMessage(CURRENT_SIZE);
            Log.i("zhu_test","copyFile copy success...");
            return COPY_SUCCESS;
        } catch (Exception ex) {
            Log.i("zhu_test","copyFile copy fail..."+ex.getLocalizedMessage());
            return COPY_FAIL;
        }
    }
    public void copyMedia(){
        startCopy(FROMPATH_MEDIA_UDISK,TOPATH_MEDIA_UDISK);
    }
    public void copyMusic(){
        startCopy(FROMPATH_MUSIC_UDISK,TOPATH_MUSIC_UDISK);
    }
    public void copyPhoto(){
        startCopy(FROMPATH_PHOTO_UDISK,TOPATH_PHOTO_UDISK);
    }
}
