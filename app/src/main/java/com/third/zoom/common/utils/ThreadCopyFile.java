package com.third.zoom.common.utils;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;

public class ThreadCopyFile extends Thread {
    //    public static String path = "G:\\Media\\城中大盗cd2.rmvb";
//    public static String topath = "G:\\城中大盗cd2.rmvb";
    private String fromPath;
    private String toPath;
    private long from;   //copy起始位置
    private long to;     //copy结束位置

    public ThreadCopyFile(String fromPath, String toPath, long from, long to) {
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.from = from;
        this.to = to;
    }

    public void run() {
        try {
            InputStream in = new FileInputStream(fromPath);  //copy视频类型文件用字节流
            RandomAccessFile out = new RandomAccessFile(toPath, "rw");
            in.skip(from);          //跳一定的字节后再开始读取
            out.seek(from);         //从一定字节后开始写入
            long sumbyte = 0;       //统计读取了多少个字节数
            byte[] buff = new byte[1024 * 1024];
            int len = 0;
            //读取的字节数必须有数量限制 限制小于 to 和from 的差
            while ((len = in.read(buff)) != -1 && sumbyte <= (to - from)) {
                out.write(buff, 0, len);
                sumbyte += len;
            }
            in.close();
            out.close();
            Date date2 = new Date();
            System.out.println(Thread.currentThread().getName() + " 完成时间为：" + date2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
