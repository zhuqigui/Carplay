package com.third.zoom.common.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.third.zoom.common.utils.FileUtils;
import com.third.zoom.common.utils.PreferenceUtils;

/**
 * 作者：Sky on 2018/3/5.
 * 用途：基类
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG="zhu_test";
    protected ActivityFragmentInject annotation;
    FileUtils mFileUtils;
    private ProgressDialog progressDialog;
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            toHandleMessage(msg);
        }
    };

    /**
     * handler消息处理
     * @param msg
     */
    protected abstract void toHandleMessage(Message msg);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getClass().isAnnotationPresent(ActivityFragmentInject.class)) {
            throw new RuntimeException("must use ActivityFragmentInitParams.class");
        }
        annotation = getClass().getAnnotation(ActivityFragmentInject.class);
        setContentView(annotation.contentViewId());
        findViewAfterViewCreate();
        initDataAfterFindView();
        mFileUtils=new FileUtils();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * view初始化
     */
    protected abstract void findViewAfterViewCreate();

    /**
     * 数据初始化
     */
    protected abstract void initDataAfterFindView();
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG,"BaseActivity onKeyDown keyCode=="+keyCode);
        if(keyCode==14){
            Log.i("zhu_test","keyCode==14 start copy media");
            //mFileUtils.copyMedia();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        mFileUtils.copyMedia();
                        progressDialog.dismiss();//关闭进程对话框
                        Toast.makeText(BaseActivity.this,"拷贝视频已完成",Toast.LENGTH_LONG).show();
                        //runOnUiThread(finishDialog);//要求运行在UI线程
                    } catch (Exception e) {}
                }
            }).start();
            progressDialog= ProgressDialog.show(BaseActivity.this, "请稍等", "正在拷贝视频中，请不要插拔U盘或SD卡...", true);
        }else if(keyCode==15){
            Log.i(TAG,"keyCode==15 start copy music");
            //mFileUtils.copyMedia();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        mFileUtils.copyMusic();
                        progressDialog.dismiss();//关闭进程对话框
                        Toast.makeText(BaseActivity.this,"拷贝音乐已完成",Toast.LENGTH_LONG).show();
                        //runOnUiThread(finishDialog);//要求运行在UI线程
                    } catch (Exception e) {}
                }
            }).start();
            progressDialog= ProgressDialog.show(BaseActivity.this, "请稍等", "正在拷贝音乐中，请不要插拔U盘或SD卡...", true);
        }else if(keyCode==16){
            Log.i(TAG,"keyCode==16 start copy photo");
            //mFileUtils.copyMedia();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        mFileUtils.copyPhoto();
                        progressDialog.dismiss();//关闭进程对话框
                        Toast.makeText(BaseActivity.this,"拷贝图片已完成",Toast.LENGTH_LONG).show();
                        //runOnUiThread(finishDialog);//要求运行在UI线程
                    } catch (Exception e) {}
                }
            }).start();
            progressDialog= ProgressDialog.show(BaseActivity.this, "请稍等", "正在拷贝图片中，请不要插拔U盘或SD卡..", true);
        }else if(keyCode==82){
            //menu键退出系统
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }
}
