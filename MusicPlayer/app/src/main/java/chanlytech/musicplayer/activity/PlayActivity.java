package chanlytech.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chanlytech.musicplayer.R;
import chanlytech.musicplayer.adapter.FragmentViewPagerAdapter;
import chanlytech.musicplayer.base.MyApploctin;
import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.fragment.CoverFragment;
import chanlytech.musicplayer.fragment.LyricFragment;
import chanlytech.musicplayer.utlis.LRCAnalysis;
import chanlytech.musicplayer.utlis.MyMedialUtil;
import chanlytech.musicplayer.utlis.PlayBgShape;
import chanlytech.musicplayer.view.LRCTextView;

/**
 * 播放界面
 */
public class PlayActivity extends FragmentActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private ViewPager mViewPager;
    private TextView start_time, end_time, music_name, musci_singer;
    private ImageView play_grey, play_pause, next_grey, iv_collection, play_modle;
    private SeekBar mSeekBar;
    private List<Fragment> mFragments = new ArrayList<>();
    private ArrayList<MusicEntity> musicEntities = new ArrayList<>();
    private int postion;
    private String TAG = "chanlytech.musicplayer.activity.PlayActivity";
    private MyMedialUtil myMedialUtil;
    private int currentPosition;
    private ProgressReceiver progressReceiver;
    private LinearLayout mLinearLayout;
    private RelativeLayout mRelativeLayout_grey, mRelativeLayout_pause, mRelativeLayout_next;
    private int mDrawable[] = {R.drawable.road_bg, R.drawable.road_blue
            , R.drawable.road_gray,R.drawable.road_green, R.drawable.road_orange,
            R.drawable.road_red, R.drawable.road_violet};
    private int prossDrawable[]={R.drawable.xml_appwidget_play_progress,R.drawable.xml_progress_blue
    ,R.drawable.xml_progress_gray,R.drawable.xml_progress_green
    ,R.drawable.xml_progress_orange,R.drawable.xml_progress_red
    ,R.drawable.xml_progress_violet};
    public interface MusicPlayStatusLinster {
        void isPlaying(boolean isplaying);
    }

    private static MusicPlayStatusLinster musicPlayStatusLinster;
    private MyTimer myTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Android5.0全透明状态栏效果
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_play);
        initView();
        initData();
        initLinster();


    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        start_time = (TextView) findViewById(R.id.tv_satrt_time);
        end_time = (TextView) findViewById(R.id.tv_end_time);
        music_name = (TextView) findViewById(R.id.music_name);
        musci_singer = (TextView) findViewById(R.id.music_singer);
        mSeekBar = (SeekBar) findViewById(R.id.sb);
        play_grey = (ImageView) findViewById(R.id.play_grey);//上一曲
        play_pause = (ImageView) findViewById(R.id.play_pause);//暂停
        next_grey = (ImageView) findViewById(R.id.next_grey);//下一曲
        iv_collection = (ImageView) findViewById(R.id.iv_collection);//最喜欢的歌曲
        play_modle = (ImageView) findViewById(R.id.play_modle);//更改歌曲的播放模式
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_all);
        mRelativeLayout_grey = (RelativeLayout) findViewById(R.id.rl_paly_grey);
        mRelativeLayout_pause = (RelativeLayout) findViewById(R.id.rl_play_pause);
        mRelativeLayout_next = (RelativeLayout) findViewById(R.id.rl_next_grey);
    }

    private void initLinster() {
        play_grey.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        next_grey.setOnClickListener(this);
        iv_collection.setOnClickListener(this);
        play_modle.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

    }

    private void initData() {
        musicEntities = getIntent().getParcelableArrayListExtra("musiclist");
        postion = getIntent().getIntExtra("position", 0);
        myMedialUtil = new MyMedialUtil(this, musicEntities);
        mFragments.add(CoverFragment.newInstance());
        mFragments.add(LyricFragment.newInstance(musicEntities, postion));
        end_time.setText(musicEntities.get(postion).getMusic_time());
        start_time.setText("00:00");
        music_name.setText(musicEntities.get(postion).getMusic_name());
        musci_singer.setText(musicEntities.get(postion).getMusic_singer());
        FragmentViewPagerAdapter fragmentViewPagerAdapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager(), mViewPager, mFragments);
        mSeekBar.setMax(musicEntities.get(postion).getTime());
        myMedialUtil.play(postion, 0);
        myTimer = new MyTimer(30000, 1000);
        myTimer.start();
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        Bitmap bitmap = Bitmap.createBitmap(mBitmap, 0, 0, MyApploctin.sScreenWidth, MyApploctin.sScreenHeight);
        mLinearLayout.setBackgroundDrawable(new ShapeDrawable(new PlayBgShape(bitmap)));
//        Drawable drawable=getResources().getDrawable(R.drawable.handleview_layout_seekbar_fg);
//        mSeekBar.setProgressDrawable();


    }

    /**
     * 随机颜色
     */
    private int getRandomDrawable() {
        int random = (int) (Math.random() * (mDrawable.length));
        return random;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_grey://上一曲
                myMedialUtil.playPrevious();
                CoverFragment.creat();
                if (myMedialUtil.isPlaying) {
                    play_pause.setBackgroundResource(R.mipmap.img_appwidget91_voice_pause_normal);
                }
                musicPlayStatusLinster.isPlaying(myMedialUtil.isPlaying);
                break;
            case R.id.play_pause://播放和暂停
                if (myMedialUtil.isPlaying) {
                    myMedialUtil.pause();//暂停
                    play_pause.setBackgroundResource(R.mipmap.img_appwidget91_voice_play_normal);
                } else {
                    myMedialUtil.rePlay();//播放
                    play_pause.setBackgroundResource(R.mipmap.img_appwidget91_voice_pause_normal);
                }
                musicPlayStatusLinster.isPlaying(myMedialUtil.isPlaying);
                break;
            case R.id.next_grey://下一曲
                myMedialUtil.playNext();
                CoverFragment.creat();
                if (myMedialUtil.isPlaying) {
                    play_pause.setBackgroundResource(R.mipmap.img_appwidget91_voice_pause_normal);
                }
                musicPlayStatusLinster.isPlaying(myMedialUtil.isPlaying);
                break;
            case R.id.iv_collection://喜欢的歌曲
                Toast.makeText(this, getRandomDrawable() + "", Toast.LENGTH_LONG).show();
                break;
            case R.id.play_modle://歌曲的播放模式
                //默认为列表循环
                int currentMode = myMedialUtil.changeMode();
                if (currentMode == 0) {
                    //单曲循环
                    play_modle.setBackgroundResource(R.mipmap.img_appwidget_playmode_repeatone);
                } else if (currentMode == 1) {
                    //列表循环
                    play_modle.setBackgroundResource(R.mipmap.img_appwidget_playmode_sequence);
                } else if (currentMode == 2) {
                    //随机播放
                    play_modle.setBackgroundResource(R.mipmap.img_appwidget_playmode_shuffle);
                } else if (currentMode == 3) {
                    //顺序播放
                    play_modle.setBackgroundResource(R.mipmap.img_appwidget_playmode_repeat);
                }
                break;

        }
    }

    private void initReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_DURATION);
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC);
        registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTimer.cancel();
        unregisterReceiver(progressReceiver);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            myMedialUtil.setMusicPro(progress);
        }


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "seekBar触摸了");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    public static void setMusicPlayStatuLinter(MusicPlayStatusLinster Linster) {
        musicPlayStatusLinster = Linster;
    }

    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyMedialUtil.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_PROGRESS, currentPosition);
                if (progress > 0) {
                    currentPosition = progress; //记住当前位置
                    mSeekBar.setProgress(progress / 1000);
                    start_time.setText(formatTime(progress));
                }
            } else if (MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC.equals(action)) {
                //检索当前的音乐，标题和歌手显示在屏幕顶部的。
                postion = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC, 0);
                music_name.setText(musicEntities.get(postion).getMusic_name());
                musci_singer.setText(musicEntities.get(postion).getMusic_singer());
            } else if (MyMedialUtil.ACTION_UPDATE_DURATION.equals(action)) {
                //进度条在收到时间和显示
                //为什么要做这个？因为来自ContentResolver，持续时间为零。
                int duration = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_DURATION, 0);
                end_time.setText(formatTime(duration));
                mSeekBar.setMax(duration / 1000);
            }
        }
    }

    class MyTimer extends CountDownTimer {

        public MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            int random = getRandomDrawable();
            int drawable=mDrawable[random];
//            int probg=prossDrawable[random];
            Log.i("随机样式", drawable + "");
            if (MyMedialUtil.isPlaying) {
                mRelativeLayout_grey.setBackgroundResource(drawable);
                mRelativeLayout_pause.setBackgroundResource(drawable);
                mRelativeLayout_next.setBackgroundResource(drawable);
//                Drawable drawable1 = getResources().getDrawable(R.color.us_color_f53172);
//                Drawable drawable1=getResources().getDrawable(probg);
//                mSeekBar.setProgressDrawable(drawable1);
//                mSeekBar.setMinimumHeight(2);

                myTimer.start();
            }

        }
    }


}
