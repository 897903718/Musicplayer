package chanlytech.musicplayer.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chanlytech.musicplayer.base.MyApploctin;
import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.fragment.CoverFragment;

/**
 * Created by Lyy on 2016/6/3.
 * 音乐播放服务
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener{
    private SensorManager mSensorManager;

    private MediaPlayer mediaPlayer;
    private OnMusicEventListener mListener;
    private int mPlayingPosition; // 当前正在播放
    private List<MusicEntity> musicList;
    private boolean isShaking;
    public static boolean isPlaying = false;
    private int currentMode = 1; //列表循环
    public static final String[] MODE_DESC = {"单曲循环", "列表循环", "随机播放", "顺序播放"};
    public static final int MODE_ONE_LOOP = 0;//单曲循环
    public static final int MODE_ALL_LOOP = 1;//列表循环
    public static final int MODE_RANDOM = 2;//随机播放
    public static final int MODE_SEQUENCE = 3;//顺序播放
    public static  int number=0;
    private static final int updateProgress = 1;
    private static final int updateCurrentMusic = 2;
    private static final int updateDuration = 3;
    private int currentPosition;//播放进度
    private static int currentMusic;//正在播放的音乐
    private ExecutorService mProgressUpdatedListener = Executors.newSingleThreadExecutor();
    public static final String ACTION_UPDATE_PROGRESS = "chanlytech.musicplayer.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "chanlytech.musicplayer.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_MUSIC = "chanlytech.musicplayer.UPDATE_CURRENT_MUSIC";



    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        musicList= MyApploctin.musicEntities;
        if(mediaPlayer==null){
            mediaPlayer=new MediaPlayer();
            //指定流媒体的类型,不能再onCreate中设置
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //监听一个回调函数去执行音乐已经准备好并且可以开始时调用
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentPosition);
                    handler.sendEmptyMessage(updateDuration);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }




    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(isShaking) return;

            if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
                float[] values = event.values;
                if (Math.abs(values[0]) > 8 && Math.abs(values[1]) > 8
                        && Math.abs(values[2]) > 8) {
                    isShaking = true;
//                    next();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isShaking = false;
                        }
                    }, 200);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /**
     * 更新进度的线程
     */
    private Runnable mPublishProgressRunnable = new Runnable() {
        @Override
        public void run() {
            for(;;) {
                if(mediaPlayer != null && mediaPlayer.isPlaying() &&
                        mListener != null) {
                    mListener.onPublish(mediaPlayer.getCurrentPosition());
                }

                SystemClock.sleep(200);
            }
        }
    };

    /**
     * 设置回调
     * @param l
     */
    public void setOnMusicEventListener(OnMusicEventListener l) {
        mListener = l;
    }

    /**
     * 音乐播放完毕 自动下一曲
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
//        next();
        if (isPlaying) {
            CoverFragment.creat();
            switch (currentMode) {
                case MODE_ONE_LOOP://单曲循环
                    mediaPlayer.start();
                    break;
                case MODE_ALL_LOOP:///列表循环
                    play((currentMusic + 1) % musicList.size(), 0);
                    break;
                case MODE_RANDOM://随机播放
                    play(getRandomPosition(), 0);
                    break;
                case MODE_SEQUENCE://顺序播放
                    if (currentMusic < musicList.size() - 1) {
                        playNext();
                    }
                    break;
                default:
                    break;
            }
        }
    }
    public void play(int currentMusic, int pCurrentPosition) {
        number=0;
        currentPosition = pCurrentPosition;
        setCurrentMusic(currentMusic);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(musicList.get(currentMusic).getMusic_url());
//            mediaPlayer.setDataSource("/storage/emulated/0/qqmusic/song/好妹妹乐队 - 不说再见 [mqms2].m4a");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开始准备
        //在OnPreparedListener的onPrepared的方法调用start()。
//        mediaPlayer.prepareAsync();
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(updateProgress);
        isPlaying = true;
    }

    /**
     * 随机播放
     */
    private int getRandomPosition() {
        int random = (int) (Math.random() * (musicList.size() - 1));
        return random;
    }


    /**
     * 暂停
     */
    public  void pause() {
        if(mediaPlayer!=null){
            mediaPlayer.pause();
            isPlaying = false;
        }

    }

    /**
     * 继续播放
     */
    public  void rePlay() {
        if(mediaPlayer!=null){
            mediaPlayer.start();
            isPlaying = true;
            handler.sendEmptyMessage(updateProgress);
        }

    }

    /**
     * 设置播放第几首
     */
    public void setCurrentMusic(int pCurrentMusic) {
        currentMusic = pCurrentMusic;
        handler.sendEmptyMessage(updateCurrentMusic);
    }

    /**
     * 设置循环模式
     */
    public int changeMode() {
        currentMode = (currentMode + 1) % 4;
        Toast.makeText(this, MODE_DESC[currentMode], Toast.LENGTH_SHORT).show();
        return currentMode;
    }
    /**
     * 获取正在播放的位置
     * @return
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }
    /**
     * 设置播放进度
     */
    public void setMusicPro(int pCurrentMusic) {
        if (mediaPlayer != null) {
            currentPosition = pCurrentMusic * 1000;
            if (isPlaying) {
                mediaPlayer.seekTo(currentPosition);
            } else {
                play(currentMusic, currentPosition);
            }
        }
    }

    /**
     * 下一曲
     */
    public void playNext() {

        switch (currentMode) {
            case MODE_ONE_LOOP:
                play(currentMusic, 0);
                break;
            case MODE_ALL_LOOP://列表循环
                if (currentMusic + 1 == musicList.size()) {
                    play(0, 0);
                } else {
                    play(currentMusic + 1, 0);
                }
                break;
            case MODE_SEQUENCE://顺序
                if (currentMusic + 1 == musicList.size()) {
                    Toast.makeText(this, "No more song.", Toast.LENGTH_SHORT).show();
                } else {
                    play(currentMusic + 1, 0);
                }
                break;
            case MODE_RANDOM://随机
                play(getRandomPosition(), 0);
                break;
        }
    }

    /**
     * 上一曲
     */
    public void playPrevious() {

        switch (currentMode) {
            case MODE_ONE_LOOP:
                play(currentMusic, 0);
                break;
            case MODE_ALL_LOOP:
                if (currentMusic - 1 < 0) {
                    play(musicList.size() - 1, 0);
                } else {
                    play(currentMusic - 1, 0);
                }
                break;
            case MODE_SEQUENCE:
                if (currentMusic - 1 < 0) {
                    Toast.makeText(this, "No previous song.", Toast.LENGTH_SHORT).show();
                } else {
                    play(currentMusic - 1, 0);
                }
                break;
            case MODE_RANDOM:
                play(getRandomPosition(), 0);
                break;
        }
    }
    //接受子线程发送的数据, 并用此数据配合主线程更新UI
    private  Handler handler = new Handler() {
        //接受子线程传过来的(子线程用sedMessage()方法传递)Message对象，(里面包含数据)
        // 把这些消息放入主线程队列中，配合主线程进行更新UI
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case updateProgress:
                    toUpdateProgress();
                    break;
                case updateDuration:
                    toUpdateDuration();
                    break;
                case updateCurrentMusic://更新第几首音乐
                    toUpdateCurrentMusic();
                    break;
            }
        }
    };

    /**
     * 更新进度
     */
    private  void toUpdateProgress() {
        if (mediaPlayer != null && isPlaying) {
            int progress = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_PROGRESS);
            intent.putExtra(ACTION_UPDATE_PROGRESS, progress);
            intent.putExtra(ACTION_UPDATE_DURATION, duration);
            sendBroadcast(intent);
            handler.sendEmptyMessageDelayed(updateProgress, 100);    //设置延迟
        }
    }

    /**
     * 更新音乐时长
     */
    private  void toUpdateDuration() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_DURATION);
            intent.putExtra(ACTION_UPDATE_DURATION, duration);
            sendBroadcast(intent);
        }
    }

    /**
     * 更新第几首歌
     */
    private  void toUpdateCurrentMusic() {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_CURRENT_MUSIC);
        intent.putExtra(ACTION_UPDATE_CURRENT_MUSIC, currentMusic);
        sendBroadcast(intent);
    }

    /**
     * 音乐播放回调接口
     * @author qibin
     */
    public interface OnMusicEventListener {
        public void onPublish(int percent);
        public void onChange(int position);
    }
}
