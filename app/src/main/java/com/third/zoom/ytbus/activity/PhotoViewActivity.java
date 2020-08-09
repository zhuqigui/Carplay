package com.third.zoom.ytbus.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.third.zoom.R;
import com.third.zoom.ytbus.bean.Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2020/8/2.
 */
public class PhotoViewActivity extends AppCompatActivity {

    List<Info> mList = new ArrayList<>();
    CycleViewPager mCycleViewPager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_view);
        initData();
        initView();
    }

    private void initView() {
        mCycleViewPager = (CycleViewPager) findViewById(R.id.cycle_view);
        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        //设置轮播间隔时间，默认为4000
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, mAdCycleViewListener);
    }

    /**
     * 轮播图点击监听
     */
    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(Info info, int position, View imageView) {

            if (mCycleViewPager.isCycle()) {
                position = position - 1;
            }
            Toast.makeText(PhotoViewActivity.this, info.getTitle() + "选择了--" + position, Toast.LENGTH_LONG).show();
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        ArrayList<Uri> imageUris = getImageUris();
        for(Uri uri:imageUris) {
            String path = getRealFilePath(uri);
            Log.d("fubc","path="+path);
            mList.add(new Info(path));
        }
    }

    private static Bitmap decodeSampleBitmap(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }


    public static View getImageView(Context context, String path) {
        RelativeLayout rl = new RelativeLayout(context);
        //添加一个ImageView，并加载图片
        ImageView imageView = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(layoutParams);
        //使用Picasso来加载图片
        //Picasso.with(context).load(url).into(imageView);
        imageView.setImageBitmap(decodeSampleBitmap(path));
        //在Imageview前添加一个半透明的黑色背景，防止文字和图片混在一起
        ImageView backGround = new ImageView(context);
        backGround.setLayoutParams(layoutParams);
        backGround.setBackgroundResource(R.color.cycle_image_bg);
        rl.addView(imageView);
        rl.addView(backGround);
        return rl;
    }

    private ArrayList<Uri> getImageUris(){
        Cursor c = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        ArrayList<Uri> imageUris = new ArrayList<>();
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    int imgId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID));
                    Uri path = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imgId);
                    imageUris.add(path);
                }
                    c.close();
            } catch (Exception e) {
                if (!c.isClosed()) c.close();
            }
        }
        return imageUris;
    }


    public String getRealFilePath(Uri uri ) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private void toPhotoList() {
        Intent toPhotoList = new Intent(this,PhotoListActivity.class);
        //toDetail.putExtra("urlPath",PreferenceUtils.getString("selectPath",""));
        startActivityForResult(toPhotoList,3);
    }
    private void toMediaActivity() {
        Intent toMedia = new Intent(this,MainActivity.class);
        startActivity(toMedia);
    }
    private void toMusicActivity() {
        Intent toMedia = new Intent(this,MusicPlayActivity.class);
        startActivity(toMedia);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("fubc","photo...onKeyDown = " + keyCode);
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
            toPhotoList();
            return true;
        }else if(keyCode==8){
            toMediaActivity();
            Log.d("fubc","photo...onKeyDown = 8 to MediaActivity...");
            return true;
        }else if(keyCode==9){
            toMusicActivity();
            Log.d("fubc","photo...onKeyDown = 8 to MusicActivity...");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
