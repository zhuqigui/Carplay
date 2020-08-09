package com.third.zoom.ytbus.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.third.zoom.R;
import com.third.zoom.common.base.ActivityFragmentInject;
import com.third.zoom.common.base.BaseActivity;
import com.third.zoom.common.utils.FileUtils;
import com.third.zoom.common.utils.KeyEventUtils;
import com.third.zoom.common.utils.PreferenceUtils;
import com.third.zoom.common.utils.SpaceFileUtil;
import com.third.zoom.common.widget.MediaListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

@ActivityFragmentInject(
        contentViewId = R.layout.test_activity,
        hasNavigationView = false,
        hasToolbar = false
)

public class PlayListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener,ListView.OnFocusChangeListener{//
    private static final String TAG ="zhu_test" ;
    private static final int UPDATE_LISTVIEW_ADAPTER =1;
    private List<VideoInfo> mVideoInfos;
    MediaListView lvLocalVideoList;
    VideoInfoComparator mVideoInfoComparator;
    VideoAdapter mVideoAdapter;
    int selectItem=-1;

    private static final String[] sLocalVideoColumns = {
            MediaStore.Video.Media._ID, // 视频id
            MediaStore.Video.Media.DATA, // 视频路径
            MediaStore.Video.Media.SIZE, // 视频字节大小
            MediaStore.Video.Media.DISPLAY_NAME, // 视频名称 xxx.mp4
            MediaStore.Video.Media.TITLE, // 视频标题
            MediaStore.Video.Media.DATE_ADDED, // 视频添加到MediaProvider的时间
            MediaStore.Video.Media.DATE_MODIFIED, // 上次修改时间，该列用于内部MediaScanner扫描，外部不要修改
            MediaStore.Video.Media.MIME_TYPE, // 视频类型 video/mp4
            MediaStore.Video.Media.DURATION, // 视频时长
            MediaStore.Video.Media.ARTIST, // 艺人名称
            MediaStore.Video.Media.ALBUM, // 艺人专辑名称
            MediaStore.Video.Media.RESOLUTION, // 视频分辨率 X x Y格式
            MediaStore.Video.Media.DESCRIPTION, // 视频描述
            MediaStore.Video.Media.IS_PRIVATE,
            MediaStore.Video.Media.TAGS,
            MediaStore.Video.Media.CATEGORY, // YouTube类别
            MediaStore.Video.Media.LANGUAGE, // 视频使用语言
            MediaStore.Video.Media.LATITUDE, // 拍下该视频时的纬度
            MediaStore.Video.Media.LONGITUDE, // 拍下该视频时的经度
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.MINI_THUMB_MAGIC,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
    };
    private static final String[] sLocalVideoThumbnailColumns = {
            MediaStore.Video.Thumbnails.DATA, // 视频缩略图路径
            MediaStore.Video.Thumbnails.VIDEO_ID, // 视频id
            MediaStore.Video.Thumbnails.KIND,
            MediaStore.Video.Thumbnails.WIDTH, // 视频缩略图宽度
            MediaStore.Video.Thumbnails.HEIGHT // 视频缩略图高度
    };

    @Override
    protected void toHandleMessage(Message msg) {
        switch (msg.what){
            case UPDATE_LISTVIEW_ADAPTER:
                initVideoData();
                mVideoAdapter=new VideoAdapter(this, mVideoInfos);
                lvLocalVideoList.setAdapter(mVideoAdapter);
                //mVideoAdapter.setSelectItem(PreferenceUtils.getInt("play_list_position",0)); //自定义的变量，以便让adapter知道要选中哪一项
                //lvLocalVideoList.setItemChecked(PreferenceUtils.getInt("play_list_position",0),true);
                Log.i(TAG,"UPDATE_LISTVIEW_ADAPTER selectItem=="+selectItem);
                selectItem=PreferenceUtils.getInt("play_list_position",0);
                lvLocalVideoList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //lvLocalVideoList.requestFocus(selectItem);
                        //mVideoAdapter.notifyDataSetChanged();//提醒数据已经变动
                        Log.i("zhu_test","UPDATE_LISTVIEW_ADAPTER PreferenceUtils.getBoolean(\"is_first_create_list\",false).."+PreferenceUtils.getBoolean("is_first_create_list",false));
                        //第一次创建播放列表存储is_first_create_list为true,
                        if(!PreferenceUtils.getBoolean("is_first_create_list",false)){
                            KeyEventUtils.sendKeyEvent(20);
                            PreferenceUtils.commitBoolean("is_first_create_list",true);
                        }
                        lvLocalVideoList.setSelector(R.color.bg_pressed);
                        lvLocalVideoList.setSelectionFromTop(selectItem,0);
                        //lvLocalVideoList.setSelection(selectItem);
//                        mVideoAdapter.setSelectItem(selectItem);
                        //lvLocalVideoList.setItemChecked(selectItem,true);
                        Log.i(TAG,"lvLocalVideoList.setSelection selectItem=="+selectItem);
                    }
                },100);
                //mVideoAdapter.notifyDataSetInvalidated();//提醒数据已经变动
                Log.i(TAG,"mVideoInfos.size()=="+mVideoInfos.size()+",PreferenceUtils.getInt(\"play_list_position\",0)=="+PreferenceUtils.getInt("play_list_position",0));
                break;
                default:
                    break;
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.test_activity);
//        btnCopyMedia=(Button)this.findViewById(R.id.btn_copy_media);
//        btnCopyMusic=(Button)this.findViewById(R.id.btn_copy_music);
//        btnCopyPhoto=(Button)this.findViewById(R.id.btn_copy_photo);
//        btnCopyMedia.setOnClickListener(this);
//        btnCopyMusic.setOnClickListener(this);
//        btnCopyPhoto.setOnClickListener(this);
//        mFileUtils=new FileUtils();
        Log.i(TAG,"onCreate...");
    }
    @Override
    protected void findViewAfterViewCreate() {
        Log.i(TAG,"initDataAfterFindView...");
        lvLocalVideoList = (MediaListView)findViewById(R.id.lv_local_video_list);
        lvLocalVideoList.setOnItemClickListener(this);
        lvLocalVideoList.setOnItemSelectedListener(this);
        //lvLocalVideoList.setOnFocusChangeListener(this);
    }

    @Override
    protected void initDataAfterFindView() {
        mVideoInfos = new ArrayList<>();
//        if(!isFirstInit){
//            //从
//        }
        //mHandler.sendEmptyMessageDelayed(UPDATE_LISTVIEW_ADAPTER,500);
        mHandler.sendEmptyMessage(UPDATE_LISTVIEW_ADAPTER);
        Log.i(TAG,"initDataAfterFindView send UPDATE_LISTVIEW_ADAPTER ...");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //mVideoAdapter.setSelectItem(i); //自定义的变量，以便让adapter知道要选中哪一项
        //lvLocalVideoList.setItemChecked()
        //mVideoAdapter.notifyDataSetInvalidated();//提醒数据已经变动
        PreferenceUtils.commitInt("play_list_position",i);
        String path=mVideoInfos.get(i).data;
        String title=mVideoInfos.get(i).title;
        Log.i(TAG,"onItemClick i=="+i+",path=="+path);
        int type = SpaceFileUtil.getFileType(path);
        if(type >= 300 && type < 400) {
            //不合规的文件不让选择
//            boolean check = checkDir(path);
//            if(!check){
//                Toast.makeText(FileSystemActivity.this,"Please select folder names: VIDEO1 - VIDEO12!",Toast.LENGTH_LONG).show();
//                return;
//            }

            Intent mIntent = new Intent();
            //String relPath = path.replace(urlPath, "");
            mIntent.putExtra("selectPath", path);
            mIntent.putExtra("title", title);
            setResult(1, mIntent);
            //Toast.makeText(PlayListActivity.this, "will play movie:" + path, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        //selectItem=position;

        //lvLocalVideoList.setSelection(selectItem);
        //lvLocalVideoList.setItemChecked(selectItem,true);
//        lvLocalVideoList.clearChoices();
//        lvLocalVideoList.clearFocus();
        //Log.i(TAG,"onItemSelected lvLocalVideoList.clearChoices() position=="+position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.i(TAG,"onNothingSelected ...");
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        Log.i(TAG,"onFocusChange ...b=="+b);
//            if(b){
//                view.setBackgroundResource(R.color.bg_pressed);
//            }else{
//                view.setBackgroundColor(Color.TRANSPARENT);
//            }
    }

    private class VideoAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<VideoInfo> mVideoInfos;

        VideoAdapter(Context context, List<VideoInfo> videoInfos) {
            this.mVideoInfos = videoInfos;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mVideoInfos.size();
        }

        @Override
        public VideoInfo getItem(int position) {
            return mVideoInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.i(TAG,"getView position=="+position+",selectItem=="+selectItem);
//            setSelectItem(selectItem);
//            notifyDataSetChanged();
            //changeMedia(position);
            VideoInfoHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.local_video_list_item, parent, false);
                holder = new VideoInfoHolder();
                holder.ivData = (ImageView) convertView.findViewById(R.id.iv_data);
                holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
                holder.tvAlbum = (TextView)convertView.findViewById(R.id.tv_album);
                convertView.setTag(holder);
            } else {
                holder = (VideoInfoHolder) convertView.getTag();
            }
//            lvLocalVideoList.setSelection(selectItem);
//            notifyDataSetChanged();
//            if(selectItem==position){
//                convertView.setBackgroundResource(R.color.bg_pressed);
//            }else{
//                convertView.setBackgroundColor(Color.TRANSPARENT);
//            }
            VideoInfo videoInfo = getItem(position);
            //holder.ivData.setImageBitmap(BitmapFactory.decodeFile(videoInfo.thumbnailData));
            //解决获取不到缩略图问题
            holder.ivData.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoInfo.data,MINI_KIND));
            holder.tvArtist.setText(videoInfo.title);
            holder.tvAlbum.setText(videoInfo.data);

            return convertView;
        }
        public void setSelectItem(int selectItem) {
            selectItem = selectItem;
        }
        private final class VideoInfoHolder {
            ImageView ivData;
            TextView tvArtist;
            TextView tvAlbum;
        }
    }

    private void initVideoData() {


        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, sLocalVideoColumns,
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                VideoInfo videoInfo = new VideoInfo();

                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                long dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
                String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));
                String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION));
                String description = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION));
                int isPrivate = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE));
                String tags = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TAGS));
                String category = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.CATEGORY));
                double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE));
                int dateTaken = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN));
                int miniThumbMagic = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC));
                String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
                String bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                int bookmark = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK));

                Cursor thumbnailCursor = getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, sLocalVideoThumbnailColumns,
                        MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
                if (thumbnailCursor != null && thumbnailCursor.moveToFirst()) {
                    do {
                        String thumbnailData = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                        int kind = thumbnailCursor.getInt(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.KIND));
                        long width = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.WIDTH));
                        long height = thumbnailCursor.getLong(thumbnailCursor.getColumnIndex(MediaStore.Video.Thumbnails.HEIGHT));

                        videoInfo.thumbnailData = thumbnailData;
                        videoInfo.kind = kind;
                        videoInfo.width = width;
                        videoInfo.height = height;
                    } while (thumbnailCursor.moveToNext());

                    thumbnailCursor.close();
                }

                videoInfo.id = id;
                videoInfo.data = data;
                videoInfo.size = size;
                videoInfo.displayName = displayName;
                videoInfo.title = title;
                videoInfo.dateAdded = dateAdded;
                videoInfo.dateModified = dateModified;
                videoInfo.mimeType = mimeType;
                videoInfo.duration = duration;
                videoInfo.artist = artist;
                videoInfo.album = album;
                videoInfo.resolution = resolution;
                videoInfo.description = description;
                videoInfo.isPrivate = isPrivate;
                videoInfo.tags = tags;
                videoInfo.category = category;
                videoInfo.latitude = latitude;
                videoInfo.longitude = longitude;
                videoInfo.dateTaken = dateTaken;
                videoInfo.miniThumbMagic = miniThumbMagic;
                videoInfo.bucketId = bucketId;
                videoInfo.bucketDisplayName = bucketDisplayName;
                videoInfo.bookmark = bookmark;

                //Log.v(TAG, "videoInfo = " + videoInfo.toString());
                //Log.v(TAG, "sort before videoInfo.title = " + videoInfo.title);
                //不添加广告视频
                if(!(videoInfo.data.contains("AD") || videoInfo.data.contains("AD00")))
                mVideoInfos.add(videoInfo);
            } while (cursor.moveToNext());
            //按影片标题进行排序（降序）
            mVideoInfoComparator=new VideoInfoComparator();
            Collections.sort(mVideoInfos,mVideoInfoComparator);
            //先保存到
            //if()
//            for (int i = 0; i <mVideoInfos.size(); i++) {
//                Log.v(TAG,"sort after mVideoInfos.get(i).title=="+mVideoInfos.get(i).title+",i=="+i);
//            }
            cursor.close();
        }
    }
    class VideoInfoComparator implements Comparator<VideoInfo>{
        @Override
        public int compare(VideoInfo v1, VideoInfo v2) {
            return v1.title.compareTo(v2.title);
        }
    }
    private static final class VideoInfo{
        private int id;
        private String data;
        private long size;
        private String displayName;
        private String title;
        private long dateAdded;
        private long dateModified;
        private String mimeType;
        private long duration;
        private String artist;
        private String album;
        private String resolution;
        private String description;
        private int isPrivate;
        private String tags;
        private String category;
        private double latitude;
        private double longitude;
        private int dateTaken;
        private int miniThumbMagic;
        private String bucketId;
        private String bucketDisplayName;
        private int bookmark;

        private String thumbnailData;
        private int kind;
        private long width;
        private long height;

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "id=" + id +
                    ", data='" + data + '\'' +
                    ", size=" + size +
                    ", displayName='" + displayName + '\'' +
                    ", title='" + title + '\'' +
                    ", dateAdded=" + dateAdded +
                    ", dateModified=" + dateModified +
                    ", mimeType='" + mimeType + '\'' +
                    ", duration=" + duration +
                    ", artist='" + artist + '\'' +
                    ", album='" + album + '\'' +
                    ", resolution='" + resolution + '\'' +
                    ", description='" + description + '\'' +
                    ", isPrivate=" + isPrivate +
                    ", tags='" + tags + '\'' +
                    ", category='" + category + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", dateTaken=" + dateTaken +
                    ", miniThumbMagic=" + miniThumbMagic +
                    ", bucketId='" + bucketId + '\'' +
                    ", bucketDisplayName='" + bucketDisplayName + '\'' +
                    ", bookmark=" + bookmark +
                    ", thumbnailData='" + thumbnailData + '\'' +
                    ", kind=" + kind +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
}
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_copy_media:
                Log.i("zhu_test","copy media...");

                break;
            case R.id.btn_copy_music:

                break;
            case R.id.btn_copy_photo:

                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"PlayListActivity onDestroy.....");
        super.onDestroy();
    }
}
