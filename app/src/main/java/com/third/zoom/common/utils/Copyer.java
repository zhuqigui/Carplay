package com.third.zoom.common.utils;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;

/**
 * 该类执行文件的拷贝功能
 * @author haokui
 *
 */
class Copyer {
    private static final String TAG="zhu_test";
    private CopyThread[] threads;// 存放所有拷贝线程的数组

    /**
     * 使用多线程去拷贝一个大文件, 1 在使用多线程进行拷贝的时候,首先要知道文件的大小 然后根据线程的数量,计算出每个线程的工作的数量
     * 2.然后创建线程,执行拷贝的工作
     *
     * @param srcFile
     *            源文件
     * @param desPath
     *            目标路径
     * @param threadNum
     *            要使用的线程数量
     */
    public  void copy(File srcFile, String desPath, int threadNum) {
        // 1.取得文件的大小
        long fileLeng = srcFile.length();
        Log.i(TAG,"copy 文件大小:" + fileLeng);

        // 2.根据线程数量,计算每个线程的工作量
        long threadPerSize = fileLeng / threadNum;

        // 3.计算出每个线程的开始位置和结束位置
        long startPos = 0;
        long endPos = threadPerSize;

        // 取得目标文件的文件名信息
        String fileName = srcFile.getName();
        String desPathAndFileName = desPath + File.separator + fileName;

        // 初始化线程的数组
        threads = new CopyThread[threadNum];

        for (int i = 0; i < threadNum; i++) {
            // 由最后一个线程承担剩余的工作量
            if (i == threadNum - 1) {
                threads[i] = new CopyThread("拷贝线程" + i, srcFile,
                        desPathAndFileName, startPos, fileLeng);
            } else {
                // 创建一个线程
                threads[i] = new CopyThread("拷贝线程" + i, srcFile,
                        desPathAndFileName, startPos, endPos);
            }
            startPos += threadPerSize;
            endPos += threadPerSize;

        }

        // 创建统计线程
        new ScheduleThread("统计线程", fileLeng,threads );
    }
}
/**
 * 负责统计文件拷贝进度的线程
 * @author haokui
 *
 */
class ScheduleThread extends Thread {
    private long fileLength; // 文件的大小
    private CopyThread[] threads;// 存放所有的拷贝线程的数组

    /**
     * 统计进度线程的构造方法
     *
     * @param name
     *            线程的名字
     * @param fileLength
     *            文件的长度
     * @param threads
     *            拷贝线程的数组
     */
    public ScheduleThread(String name, long fileLength, CopyThread[] threads) {
        super(name);
        this.fileLength = fileLength;
        this.threads = threads;

        this.start();
    }

    /**
     * 判断所有的拷贝线程是否已经结束
     *
     * @return 是否结束
     */
    private boolean isOver() {
        if (threads != null) {
            for (CopyThread t : threads) {
                if (t.isAlive()) {
                    return false;
                }
            }
        }
        return true;
    }

    public  void run() {
        while (!isOver()) {
            long totalSize = 0;
            for (CopyThread t : threads) {
                totalSize += t.getCopyedSize();
            }
            /**
             * 由于复制功能要比这些代码耗时，所以稍微延迟一下，不用计算的太频繁，最好是一个线程干完之后计算一次，这里就直接给延迟一下就ok，不做精确的处理了。
             */
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double schedule = Arith.div((double) totalSize,
                    (double) fileLength, 4);
            Log.i("zhu_test","文件的拷贝进度:===============>" + + schedule * 100
                    + "%");
        }
        Log.i("zhu_test","统计线程结束了");
    }
}
/**
 * 拷贝线程
 * @author haokui
 *
 */
class CopyThread extends Thread {
    private File srcFile;// 源文件的路径
    private String desPath;// 目标路径
    private long startPos; // 线程拷贝的开始位置
    private long endPost;// 线程拷贝的结束位置
    private long alreadyCopySize;// 线程已经拷贝的位置

    private RandomAccessFile rin; // 读取文件的随机流
    private RandomAccessFile rout;// 写入文件的随机流

    /**
     * 取得 线程已经拷贝文件的大小
     *
     * @return 线程已经拷贝文件的大小
     */
    public long getCopyedSize() {
        return alreadyCopySize - startPos;
    }

    /**
     * 线程的构造方法
     *
     * @param threadName
     *            线程的名字
     * @param srcFile
     *            源文件
     * @param desPathAndName
     *            目标文件的路径及其名称
     * @param startPos
     *            线程的开始位置
     * @param endPos
     *            线程的结束位置
     */
    public CopyThread(String threadName, File srcFile, String desPathAndName,
                      long startPos, long endPos) {
        super(threadName);
        this.srcFile = srcFile;
        this.desPath = desPath;
        this.startPos = startPos;
        this.endPost = endPos;
        this.alreadyCopySize = this.startPos;

        // System.out.println(this.getName() + "开始位置:" + startPos + " 结束位置:"
        // + endPos);

        // 初始化随机输入流,输出流
        try {
            rin = new RandomAccessFile(srcFile, "r");
            rout = new RandomAccessFile(desPathAndName, "rw");

            // 定位随机流的开始位置
            rin.seek(startPos);
            rout.seek(startPos);

            // 开始线程
            this.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("zhu_test","CopyThread error.."+e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("zhu_test","CopyThread error.."+e.getLocalizedMessage());
        }
    }

    public  void run() {
        int len = 0;
        byte[] b = new byte[1024];

        try {
            while ((alreadyCopySize < endPost) && (len = rin.read(b)) != -1) {
                alreadyCopySize = alreadyCopySize + len;
                if (alreadyCopySize >= this.endPost) {
                    int oldSize = (int) (alreadyCopySize - len);
                    len = (int) (this.endPost - oldSize);
                    alreadyCopySize = oldSize + len;
                }
                rout.write(b, 0, len);
            }
            Log.i("zhu_test",this.getName() + " 在工作: 开始位置:" + this.startPos
                            + "  拷贝了:" + (this.endPost - this.startPos)  + " 结束位置:"
                            + this.endPost);
//            System.out.println(this.getName() + " 在工作: 开始位置:" + this.startPos
//                    + "  拷贝了:" + (this.endPost - this.startPos)  + " 结束位置:"
//                    + this.endPost);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rin != null) {
                    rin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (rout != null) {
                    rout.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
/**
 * 由于Java的简单类型不能够精确的对浮点数进行运算，
 * 这个工具类提供精 确的浮点数运算，包括加减乘除和四舍五入。
 * @author haokui
 *
 */
class Arith {
    // 默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;

    // 这个类不能实例化
    private Arith() {
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1
     *            被减数
     * @param v2
     *            减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1
     *            被乘数
     * @param v2
     *            乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @param scale
     *            表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v
     *            需要四舍五入的数字
     * @param scale
     *            小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
};

