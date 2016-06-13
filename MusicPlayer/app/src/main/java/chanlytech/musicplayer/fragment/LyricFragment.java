package chanlytech.musicplayer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import chanlytech.musicplayer.R;
import chanlytech.musicplayer.base.MyApploctin;
import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.utlis.LyricDownloadManager;
import chanlytech.musicplayer.utlis.MyMedialUtil;
import chanlytech.musicplayer.view.LrcView;

/**
 * Created by Lyy on 2016/5/16.
 */
public class LyricFragment extends Fragment implements LyricDownloadManager.LyricDownSuccessLinster {
    private View mView;
    private LrcView mLyricView;
    private LyricDownloadManager mLyricDownloadManager;//歌词下载器
    private ArrayList<MusicEntity> musicEntities;
    private int postion;
    private int currentTime;//当前歌曲时间
    private int duration;//歌曲时长
    private ProgressReceiver progressReceiver;
    private String basePath = MyApploctin.lrcPath;

    public LyricFragment() {
    }

    public static LyricFragment newInstance(ArrayList<MusicEntity> musicEntities, int postion) {
        LyricFragment lyricFragment = new LyricFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("postion", postion);
        bundle.putParcelableArrayList("musicEntities", musicEntities);
        lyricFragment.setArguments(bundle);
        return lyricFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLyricDownloadManager = new LyricDownloadManager(getActivity());
        musicEntities = getArguments().getParcelableArrayList("musicEntities");
        postion = getArguments().getInt("postion");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.lyric_fragment, container, false);
        mLyricView = (LrcView) mView.findViewById(R.id.lyricview);
        readLrc(postion);
        return mView;
    }

    private void readLrc(int postion) {
        File file = new File(basePath + "/" + musicEntities.get(postion).getMusic_name()+"_" + musicEntities.get(postion).getMusic_singer()+".lrc");
        if (file.exists()) {
            try {
                mLyricView.setLrcPath(file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mLyricDownloadManager.getLrcByTT(musicEntities.get(postion).getMusic_name(), musicEntities.get(postion).getMusic_singer());
            mLyricDownloadManager.setmLyricDownSuccessLinster(this);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String lyrpath = (String) msg.obj;
                    try {
                        mLyricView.setLrcPath(lyrpath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    mLyricView.setDefaultText();
                    mLyricView.invalidate();
                    break;
            }
        }
    };


    @Override
    public void LyricDownSuccess(String path) {
        Message message = new Message();
        message.obj = path;
        message.what = 1;
        mHandler.sendMessage(message);
    }

    @Override
    public void LyricDownFiled(String str) {
        Toast.makeText(getActivity(),str,Toast.LENGTH_LONG).show();
        Message message = new Message();
        message.what = 2;
        mHandler.sendMessage(message);
    }

    /**
     * 注册广播
     */
    private void initReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_DURATION);
        intentFilter.addAction(MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC);
        getActivity().registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        initReceiver();
        Log.i("LyricFragment", "onResume");
    }

    @Override
    public void onPause() {

        super.onPause();
        Log.i("LyricFragment", "onPause");

    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(progressReceiver);
        super.onDestroy();
        Log.i("LyricFragment", "onDestroy");
//        getActivity().unregisterReceiver(progressReceiver);
    }

    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (MyMedialUtil.ACTION_UPDATE_PROGRESS.equals(action)) {
                //更新进度
                currentTime = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_PROGRESS, currentTime);
                duration = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_DURATION, duration);
                if (mLyricView.hasLrc()) mLyricView.changeCurrent(currentTime);
            } else if (MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC.equals(action)) {
                //检索当前的音乐，标题和歌手显示在屏幕顶部的。更新歌曲
                postion = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_CURRENT_MUSIC, 0);
                MyMedialUtil.number++;
                if(MyMedialUtil.number==1){
                    readLrc(postion);
                }

            } else if (MyMedialUtil.ACTION_UPDATE_DURATION.equals(action)) {
                //进度条在收到时间和显示
                //为什么要做这个？因为来自ContentResolver，持续时间为零。
                int duration = intent.getIntExtra(MyMedialUtil.ACTION_UPDATE_DURATION, 0);
            }
        }
    }
}
