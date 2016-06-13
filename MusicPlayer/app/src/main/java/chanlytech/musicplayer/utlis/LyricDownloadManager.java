
package chanlytech.musicplayer.utlis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.OnResponseListener;
import chanlytech.musicplayer.base.MyApploctin;
import chanlytech.musicplayer.entity.BaseData;
import chanlytech.musicplayer.entity.Data;
import chanlytech.musicplayer.entity.LyricData;
import chanlytech.musicplayer.entity.TianTianBaseData;
import chanlytech.musicplayer.http.LoadDatahandler;
import chanlytech.musicplayer.http.MyAsyncHttpResponseHandler;
import chanlytech.musicplayer.http.MyHttpClient;


/**
 * 歌词下载
 *
 */
public class LyricDownloadManager {
    private static final String TAG = LyricDownloadManager.class.getSimpleName();
    public static final String GBK = "UTF-8";
    public static final String UTF_8 = "utf-8";
    private URL mUrl = null;
    private RequestQueue requestQueue;
    private Context mContext = null;
    private String singer;
    private String music_name;
    //天天动听开源API接口http://search.dongting.com/song/search/old?q=%E4%B8%8D%E8%AF%B4%E5%86%8D%E8%A7%81&page=1&size=3
    //http://so.ard.iyyin.com/s/song_with_out
    /**
     * 用来标志请求的what, 类似handler的what一样，这里用来区分请求
     */
    public static final int NOHTTP_WHAT_TEST = 0x001;
    public static final int NOHTTP_WHAT_LYR = 0x002;
    public static final int NOHTTP_WHAT_T = 0x003;
    public static final int NOHTTP_WHAT_W = 0x004;
    public interface LyricDownSuccessLinster{
        void LyricDownSuccess(String path);
        void LyricDownFiled(String str);
    }

    private LyricDownSuccessLinster mLyricDownSuccessLinster;
    public LyricDownloadManager(Context c) {
        mContext = c;
        requestQueue = NoHttp.newRequestQueue();

    }

    public void getLyi(String musicName,String singer) {
        // 百度音乐盒的API
        this.singer=singer;
        if(musicName.contains("(")){
            musicName=musicName.substring(0,musicName.indexOf("("));
        }
        if(musicName.contains("（")){
            musicName=musicName.substring(0,musicName.indexOf("（"));
        }
        String strUrl = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=webapp_music&method=baidu.ting.search.catalogSug&format=jsonp&callback=&query=" + musicName + "&_=" + System.currentTimeMillis();
        Request<String> request = NoHttp.createStringRequest(strUrl, RequestMethod.GET);
//        request.add("file",new FileBinary(new File("")));上传文件
        requestQueue.add(NOHTTP_WHAT_TEST, request, onResponseListener);
    }

    public void getMusicByTianTianDongTing(String musicName,String singer ){
        //天天动听api
        this.singer=singer;
        String url="http://so.ard.iyyin.com/s/song_with_out";
        Request<String>request=NoHttp.createStringRequest(url,RequestMethod.GET);
        request.add("q",musicName);
        request.add("page",1);
        request.add("size",1);
        requestQueue.add(NOHTTP_WHAT_T,request,onResponseListener);
    }

    /**
     * 使用AsyncHttp网络请求
     * */
    public void getLrcByTT(String musicName,String singer){
        this.singer=singer;
        this.music_name=musicName;
        String url="http://search.dongting.com/song/search/old?q="+musicName+"&page=1&size=2";
        MyHttpClient.getInstance(mContext).get(url,new MyAsyncHttpResponseHandler(new LoadDatahandler(){
            @Override
            public void onSuccess(String data) {
                super.onSuccess(data);
                Log.i("天天动听api接口返回数据", data + "");
                TianTianBaseData tianTianBaseData = JSON.parseObject(data, TianTianBaseData.class);
                if (tianTianBaseData.getData() != null && tianTianBaseData.getData().size() > 0) {
                    String song_id;
                    String song_name;
                    String singer_name;
                    if(tianTianBaseData.getData().size() > 1){
                         song_id = tianTianBaseData.getData().get(1).getSong_id();
                         song_name = tianTianBaseData.getData().get(1).getSong_name();
                         singer_name = tianTianBaseData.getData().get(1).getSinger_name();
                    }else {
                         song_id = tianTianBaseData.getData().get(0).getSong_id();
                         song_name = tianTianBaseData.getData().get(0).getSong_name();
                         singer_name = tianTianBaseData.getData().get(0).getSinger_name();
                    }
                    String Lrcurl = "http://lp.music.ttpod.com/lrc/down";
                    Request<String> request = NoHttp.createStringRequest(Lrcurl, RequestMethod.GET);
                    request.add("artist", singer_name);
                    request.add("title", song_name);
                    request.add("song_id", song_id);
                    requestQueue.add(NOHTTP_WHAT_W, request, onResponseListener);
                }else {
                    if(mLyricDownSuccessLinster!=null){
                        mLyricDownSuccessLinster.LyricDownFiled("歌词获取失败");
                    }
                }
            }
            @Override
            public void onFailure(String error, String message) {
                super.onFailure(error, message);
            }
        }));

    }



    /**
     * 根据歌词下载ID，获取网络上的歌词文本内容
     *
     * @param
     */
    private String fetchLyricContent(String musicName,String singer, String lyricURL) {
        BufferedReader br = null;
        StringBuilder content = null;
        String temp = null;
        Log.i(TAG, "歌词的真实下载地址:" + lyricURL);
        try {
            mUrl = new URL(lyricURL);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
        }
        // 获取歌词文本，存在字符串类中
        try {
            // 建立网络连接
            br = new BufferedReader(new InputStreamReader(mUrl.openStream(),
                    GBK));
            if (br != null) {
                content = new StringBuilder();
                // 逐行获取歌词文本
                while ((temp = br.readLine()) != null) {
                    content.append(temp);
                    Log.i(TAG, "<Lyric>" + temp);
                }
                br.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.i(TAG, "歌词获取失败");
        }

        try {
            musicName = URLDecoder.decode(musicName, UTF_8);
//			singerName = URLDecoder.decode(singerName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (content != null) {
            String folderPath = MyApploctin.lrcPath;
            File savefolder = new File(folderPath);
            if (!savefolder.exists()) {
                savefolder.mkdirs();
            }
            String savePath = folderPath + File.separator + musicName+"_"+singer
                    + ".lrc";
//			String savePath = folderPath + File.separator + musicName + ".lrc";
            Log.i(TAG, "歌词保存路径:" + savePath);
            saveLyric(content.toString(), savePath);
            return savePath;
        } else {
            return null;
        }

    }

    /**
     * 将歌词保存到本地，写入外存中
     */
    private void saveLyric(String content, String filePath) {
        // 保存到本地
        File file = new File(filePath);
        try {
            OutputStream outstream = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(outstream);
            if(content.contains("\n")){
                content=content.replace("\n","");
            }
            content=content.replace("[", "\n[");
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "很遗憾，将歌词写入外存时发生了IO错误");
        }
        Log.i(TAG, "歌词保存成功"+filePath);
        if(mLyricDownSuccessLinster!=null){
            mLyricDownSuccessLinster.LyricDownSuccess(filePath);
        }
    }


    /**
     * 回调对象，接受请求结果
     */
    private OnResponseListener<String> onResponseListener = new OnResponseListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();// 响应结果
            switch (what){
                case NOHTTP_WHAT_TEST:
                    // 请求成功
                    Log.i("onSucceed", result + "");
                    if (result != null) {
//                    result = result.substring(1, result.length());
//                    String ss = result.substring(0, result.length() - 2);
                        if(result.endsWith("\n")){
                            result=result.replace("\n", "");
                        }
                        if(result.contains("(")){
                            result=result.substring(result.indexOf("("),result.length());
                            result= result.substring(1, result.length() - 2);
                        }
                        BaseData baseDatas = JSON.parseObject(result, BaseData.class);
                        if(baseDatas.getError_code()==22000){
                            if (baseDatas.getSong()!=null&&baseDatas.getSong().size()>0) {
                                String SongId = baseDatas.getSong().get(0).getSongid();
                                String URL = "http://music.baidu.com/data/music/links?songIds=" + SongId;
                                Log.i("URL", URL);
                                Request<String> request = NoHttp.createStringRequest(URL, RequestMethod.GET);
                                requestQueue.add(NOHTTP_WHAT_LYR, request, onResponseListener);
                            }
                        }

                    }
                    break;
                case NOHTTP_WHAT_T:
                    Log.i("天天动听api接口返回数据", result + "");
                    TianTianBaseData tianTianBaseData=JSON.parseObject(result, TianTianBaseData.class);
                    if(tianTianBaseData.getData()!=null&&tianTianBaseData.getData().size()>0){
                      String song_id=tianTianBaseData.getData().get(0).getSong_id();
                      String song_name=tianTianBaseData.getData().get(0).getSong_name();
                      String singer_name=tianTianBaseData.getData().get(0).getSinger_name();
                      String Lrcurl="http://lp.music.ttpod.com/lrc/down";
                        Request<String> request = NoHttp.createStringRequest(Lrcurl, RequestMethod.GET);
                        request.add("artist",singer_name);
                        request.add("title",song_name);
                        request.add("song_id",song_id);
                        requestQueue.add(NOHTTP_WHAT_W, request, onResponseListener);
                    }

                    break;
                case NOHTTP_WHAT_LYR:
                    // 请求成功
//                    String result = response.get();// 响应结果
                    if (result != null) {
                        LyricData baseData = JSON.parseObject(result, LyricData.class);
                        if (baseData.getData().getSongList().size() > 0) {
                            final String LrcLink = baseData.getData().getSongList().get(0).getLrcLink();//下载链接
                            final String Songname = baseData.getData().getSongList().get(0).getSongname();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if(LrcLink!=null&&LrcLink.length()>0){
                                        fetchLyricContent(Songname,singer, LrcLink);
                                    }else {
                                        Log.i(TAG,"该歌曲的路径没有找到");
                                    }

                                }
                            }).start();

                        }
                    }
                    break;
                case NOHTTP_WHAT_W:
                  Data data=JSON.parseObject(result, Data.class);
                    if(data.getData()!=null){
                        String folderPath = MyApploctin.lrcPath;
                        File savefolder = new File(folderPath);
                        if (!savefolder.exists()) {
                            savefolder.mkdirs();
                        }
                        String savePath = folderPath + File.separator +music_name+"_"+singer+".lrc";
                        Log.i("歌词",data.getData().getLrc()+"");
                        saveLyric(data.getData().getLrc(),savePath);
                    } else {
                        if(mLyricDownSuccessLinster!=null){
                            mLyricDownSuccessLinster.LyricDownFiled("歌词获取失败");
                        }
                    }
                    break;
            }


        }

        @Override
        public void onStart(int what) {
            // 请求开始，显示dialog
        }

        @Override
        public void onFinish(int what) {
            // 请求结束，关闭dialog
        }

        @Override
        public void onFailed(int i, String s, Object o, Exception e, int i1, long l) {
            Log.i("onFailed", e.toString());
            Logger.d(e);
        }
    };



    public void setmLyricDownSuccessLinster(LyricDownSuccessLinster mLyricDownSuccessLinster) {
        this.mLyricDownSuccessLinster = mLyricDownSuccessLinster;
    }
}
