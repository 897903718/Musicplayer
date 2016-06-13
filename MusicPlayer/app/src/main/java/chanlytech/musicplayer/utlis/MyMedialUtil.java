package chanlytech.musicplayer.utlis;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.fragment.CoverFragment;

/**
 * Created by Lyy on 2016/5/27.
 */
public class MyMedialUtil {
    public static MediaPlayer mediaPlayer;
    private int currentPosition;//播放进度
    private static int currentMusic;//正在播放的音乐
    public static boolean isPlaying = false;
    private ArrayList<MusicEntity> musicList;
    private static final int updateProgress = 1;
    private static final int updateCurrentMusic = 2;
    private static final int updateDuration = 3;
    private static Context mContext;
    public static final String ACTION_UPDATE_PROGRESS = "chanlytech.musicplayer.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "chanlytech.musicplayer.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_MUSIC = "chanlytech.musicplayer.UPDATE_CURRENT_MUSIC";
    private int currentMode = 1; //列表循环
    public static final String[] MODE_DESC = {"单曲循环", "列表循环", "随机播放", "顺序播放"};
    public static final int MODE_ONE_LOOP = 0;//单曲循环
    public static final int MODE_ALL_LOOP = 1;//列表循环
    public static final int MODE_RANDOM = 2;//随机播放
    public static final int MODE_SEQUENCE = 3;//顺序播放
    public static  int number=0;
    public MyMedialUtil(Context context, ArrayList<MusicEntity> musicEntities) {
        this.mContext = context;
        this.musicList = musicEntities;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
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
            //监听播放完成的事件。
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
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
            });
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
    public static void pause() {
        if(mediaPlayer!=null){
            mediaPlayer.pause();
            isPlaying = false;
        }

    }

    /**
     * 继续播放
     */
    public static void rePlay() {
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
        Toast.makeText(mContext, MODE_DESC[currentMode], Toast.LENGTH_SHORT).show();
        return currentMode;
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
                    Toast.makeText(mContext, "No more song.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(mContext, "No previous song.", Toast.LENGTH_SHORT).show();
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
    private static Handler handler = new Handler() {
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
    private static void toUpdateProgress() {
        if (mediaPlayer != null && isPlaying) {
            int progress = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_PROGRESS);
            intent.putExtra(ACTION_UPDATE_PROGRESS, progress);
            intent.putExtra(ACTION_UPDATE_DURATION, duration);
            mContext.sendBroadcast(intent);
            handler.sendEmptyMessageDelayed(updateProgress, 100);    //设置延迟
        }
    }

    /**
     * 更新音乐时长
     */
    private static void toUpdateDuration() {
        if (mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE_DURATION);
            intent.putExtra(ACTION_UPDATE_DURATION, duration);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 更新第几首歌
     */
    private static void toUpdateCurrentMusic() {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_CURRENT_MUSIC);
        intent.putExtra(ACTION_UPDATE_CURRENT_MUSIC, currentMusic);
        mContext.sendBroadcast(intent);
    }


}
