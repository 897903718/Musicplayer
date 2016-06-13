package chanlytech.musicplayer.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;

import java.io.File;
import java.util.List;

import chanlytech.musicplayer.activity.PlayActivity;
import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.server.PlayService;
import chanlytech.musicplayer.utlis.CrashHandler;
import chanlytech.musicplayer.utlis.MusicScan;
import chanlytech.musicplayer.utlis.MyMedialUtil;

/**
 * Created by Lyy on 2016/5/16.
 */
public class MyApploctin extends Application {
    private static String rootPath = "/mymusic";
    public static String lrcPath = "/lrc";
    public static int sScreenWidth;
    public static int sScreenHeight;
    private Context sContext;
    public static List<MusicEntity>musicEntities;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        initPath();
        NoHttp.initialize(this);
        Logger.setDebug(true);
        sContext = getApplicationContext();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
        phoneLinster();
        getMusicList(this);
        //开启播放服务
        startService(new Intent(this, PlayService.class));
    }

    /**
     * 创建歌词存放路径
     */
    private void initPath() {
        String ROOT = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
        }
        rootPath = ROOT + rootPath;
        lrcPath = rootPath + lrcPath;
        File lrcFile = new File(lrcPath);
        if (lrcFile.exists()) {
            lrcFile.mkdirs();
        }
    }

    /**
     * 扫描获取本地的音乐
     * */
    public  void getMusicList(Context context){
        MusicScan.initMusicScan(context);
        musicEntities=MusicScan.getMusicEntities();

    }

    /**
     * 电话来电监听
     * */
    private void phoneLinster(){
        // 添加来电监听事件
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // 获取系统服务
        telManager.listen(new MobliePhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    /**
     *
     * @author wwj
     * 电话监听器类
     */
    private class MobliePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: // 挂机状态
                    Logger.d("电话挂断了");
                    MyMedialUtil.rePlay();//继续播放
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:	//通话状态

                case TelephonyManager.CALL_STATE_RINGING:	//响铃状态
                    Logger.d("来电");
                    if(MyMedialUtil.isPlaying){
                        MyMedialUtil.pause();//暂停播放
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
