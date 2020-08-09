package com.third.zoom.ytbus.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.third.zoom.R;
import com.third.zoom.ytbus.bean.Info;
import com.third.zoom.ytbus.utils.musicUtil;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    ListView musicListView;  //lsitview展示数据的
    SeekBar mSeekBar;    //seekbar 进度条显示
    TextView mCurrentTimeTv;
    TextView mTotalTimeTv;    //歌曲总时间
    ImageView up;     //上一曲按钮
    ImageView play;    //播放暂停按钮
    ImageView next;    //下一曲按钮
    TextView now;       //当前播放歌曲名称
    private musicUtil util;  //音乐工具类，用于获取手机上的音乐
    private ArrayList<Info> musicList;  //装了音乐信息的listView
    private MusicAdapter adapter;
    private int currentposition;    //当前音乐播放位置
    private MediaPlayer mediaPlayer;
    private NotificationManager manager;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mediaPlayer.getCurrentPosition();
            mSeekBar.setProgress(progress);
            mCurrentTimeTv.setText(parseTime(progress));
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        musicListView = (ListView)findViewById(R.id.musicListView);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mCurrentTimeTv = (TextView)findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView)findViewById(R.id.total_time_tv);
        up = (ImageView)findViewById(R.id.previous);
        play = (ImageView)findViewById(R.id.play_pause);
        next = (ImageView)findViewById(R.id.next);
        now = (TextView)findViewById(R.id.now);
        mediaPlayer = new MediaPlayer();
        up.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
        getWindow().setEnterTransition(new Explode().setDuration(1000));//转场动画
        getWindow().setExitTransition(new Explode().setDuration(1000));
        initData();
    }

    private void initData() {
        util = new musicUtil();
        musicList = new ArrayList<>();
        musicList = util.getMusicInfo(this);
        adapter = new MusicAdapter();
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentposition = position;     //获取当前点击条目的位置
                changeMusic(currentposition);   //切歌
                play.setImageResource(R.drawable.pause);
                String title = musicList.get(currentposition).getTitle();
                now.setText(title);       //展示当前播放的歌名

                /*    setNotification(currentposition);*/
            }
        });

    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void changeMusic(int position) {
        if (position < 0) {
            currentposition = position = musicList.size() - 1;
        } else if (position > musicList.size() - 1) {
            currentposition = position = 0;
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        try {
            // 切歌之前先重置，释放掉之前的资源
            mediaPlayer.reset();
            // 设置播放源
            mediaPlayer.setDataSource(musicList.get(position).getUrl());
            // 开始播放前的准备工作，加载多媒体资源，获取相关信息
            mediaPlayer.prepare();

            // 开始播放
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSeekBar.setProgress(0);//将进度条初始化
        mSeekBar.setMax(mediaPlayer.getDuration());//设置进度条最大值为歌曲总时间
        mTotalTimeTv.setText(parseTime(mediaPlayer.getDuration()));//显示歌曲总时长

        updateProgress();//更新进度条
    }

    @Override
    public void onClick(View view) {
        ViewHolder viewHolder = new ViewHolder();
        if (view.getId() == R.id.previous) {//上一曲
            changeMusic(--currentposition);
            play.setImageResource(R.drawable.pause);
            String title = musicList.get(currentposition).getTitle();
            now.setText(title); //展示上一曲的歌名
        } else if (view.getId() == R.id.play_pause) {//暂停/播放
            // 首次点击播放按钮，默认播放第0首
            if (mediaPlayer == null) {
                changeMusic(0);
                String title = musicList.get(currentposition+1).getTitle();
                now.setText(title);
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause);
                }
            }
        } else if (view.getId() == R.id.next) {// 下一首
            changeMusic(++currentposition);
            play.setImageResource(R.drawable.pause);
            String title = musicList.get(currentposition).getTitle();
            now.setText(title);//展示下一首的歌名

        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            changeMusic(--currentposition);
            play.setImageResource(R.drawable.pause);
            String title = musicList.get(currentposition).getTitle();
            now.setText(title); //展示上一曲的歌名
            return true;
        }else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            changeMusic(++currentposition);
            play.setImageResource(R.drawable.pause);
            String title = musicList.get(currentposition).getTitle();
            now.setText(title);//展示下一首的歌名
            return true;
        }else  if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
            // 首次点击播放按钮，默认播放第0首
            if (mediaPlayer == null) {
                changeMusic(0);
                String title = musicList.get(currentposition+1).getTitle();
                now.setText(title);
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause);
                }
            }
            return true;
        }else if(keyCode==8){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play.setImageResource(R.drawable.play);
            }
            gotoMediaActivity();
            Log.i("zhu_test","MusicPlayActivity onKeyDown 8 pause music to play Media...");
            return true;
        }else if(keyCode==10){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play.setImageResource(R.drawable.play);
            }
            gotoPhotoActivity();
            Log.i("zhu_test","MusicPlayActivity onKeyDown 10 pause music to play photo...");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void gotoMediaActivity(){
        Intent musicIntent = new Intent(this,MainActivity.class);
        //musicIntent.
        startActivity(musicIntent);
    }
    private void gotoPhotoActivity(){
        Intent musicIntent = new Intent(this,PhotoViewActivity.class);
        //musicIntent.
        startActivity(musicIntent);
    }
    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mediaPlayer.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }

    private static final int INTERNAL_TIME = 1000;// 音乐进度间隔时间

    private String parseTime(int oldTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");// 时间格式
        String newTime = sdf.format(new Date(oldTime));
        return newTime;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        mediaPlayer.seekTo(progress);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        changeMusic(++currentposition);
        String title = musicList.get(currentposition).getTitle();
        now.setText(title);//展示下一首的歌名

    }
    public Bitmap getArtAlbum(long audioId) {
        String str = "content://media/external/audio/media/" + audioId + "/albumart";
        Uri uri = Uri.parse(str);
        ParcelFileDescriptor pfd = null;
        try {
            pfd = this.getContentResolver().openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            return null;
        }
        Bitmap bm;
        if (pfd != null) {
            FileDescriptor fd = pfd.getFileDescriptor();
            bm = BitmapFactory.decodeFileDescriptor(fd);
            return bm;
        }
        return null;
    }

    public class MusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Info getItem(int position) {
            return musicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(MusicPlayActivity.this, R.layout.music_item, null);
                viewHolder = new ViewHolder();
                viewHolder.video_imageView = (ImageView) convertView.findViewById(R.id.video_imageView);
                viewHolder.video_title = (TextView) convertView.findViewById(R.id.video_title);
                viewHolder.video_singer = (TextView)convertView.findViewById(R.id.video_singer);
                /* viewHolder.video_duration = convertView.findViewById(R.id.video_duration);*/
                //  viewHolder.video_size = convertView.findViewById(R.id.video_size);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Info item = getItem(position);
            viewHolder.video_singer.setText("歌手:" + item.getArtist());
            // viewHolder.video_size.setText((int) item.getSize());
            viewHolder.video_title.setText("歌名:" + item.getTitle());
            /* viewHolder.video_duration.setText(item.getDuration());*/
            if (getArtAlbum(item.getAbulm_id()) == null) {
                viewHolder.video_imageView.setImageResource(R.drawable.music);
            } else {
                viewHolder.video_imageView.setImageBitmap(getArtAlbum(item.getAbulm_id()));
            }

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView video_imageView;
        TextView video_title;
        TextView video_singer;
      /*  TextView video_duration;
        TextView video_size;*/
    }
}
