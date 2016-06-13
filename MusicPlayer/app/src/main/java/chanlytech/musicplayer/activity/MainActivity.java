package chanlytech.musicplayer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import chanlytech.musicplayer.R;
import chanlytech.musicplayer.adapter.MusicAdapter;
import chanlytech.musicplayer.entity.MusicEntity;
import chanlytech.musicplayer.utlis.LyricDownloadManager;
import chanlytech.musicplayer.utlis.MusicScan;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private ArrayList<MusicEntity>musicEntities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list);
        initData();
        initLinsetr();
    }

    @Override
    public void onPublish(int progress) {

    }

    @Override
    public void onChange(int position) {

    }

    private void initData(){
        mProgressDialog = ProgressDialog.show(this, null, "正在加载....");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicScan.initMusicScan(MainActivity.this);
                musicEntities=MusicScan.getMusicEntities();
                Message message=new Message();
                message.obj=musicEntities;
                message.what=1;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mProgressDialog.dismiss();
                    musicEntities= (ArrayList<MusicEntity>) msg.obj;
                    mListView.setAdapter(new MusicAdapter(MainActivity.this,musicEntities));
                    break;
            }
        }
    };

    private void initLinsetr(){
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        mPlayService.play(0,0);
        Intent mIntent=new Intent(this,PlayActivity.class);
        mIntent.putExtra("music",musicEntities.get(position));
        mIntent.putParcelableArrayListExtra("musiclist", musicEntities);
        mIntent.putExtra("position",position);
        startActivity(mIntent);
    }


}
