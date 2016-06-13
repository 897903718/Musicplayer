package chanlytech.musicplayer.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lyy on 2016/5/16.
 * 歌曲实体类
 */
public class MusicEntity implements Parcelable {
    private String id;
    private String music_name;//歌曲名
    private String music_time;//时长
    private String music_singer;//歌手
    private String music_album;//专辑
    private Bitmap music_cover_map;//封面图
    private String music_lyric;//歌词
    private String music_size;//文件大小
    private String music_url;//文件路径
    private int  time;//时长
    public  MusicEntity(){

    }


    protected MusicEntity(Parcel in) {
        id = in.readString();
        music_name = in.readString();
        music_time = in.readString();
        music_singer = in.readString();
        music_album = in.readString();
        music_cover_map = in.readParcelable(Bitmap.class.getClassLoader());
        music_lyric = in.readString();
        music_size = in.readString();
        music_url = in.readString();
        time=in.readInt();
    }

    public static final Creator<MusicEntity> CREATOR = new Creator<MusicEntity>() {
        @Override
        public MusicEntity createFromParcel(Parcel in) {
            return new MusicEntity(in);
        }

        @Override
        public MusicEntity[] newArray(int size) {
            return new MusicEntity[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMusic_name() {
        return music_name;
    }

    public void setMusic_name(String music_name) {
        this.music_name = music_name;
    }

    public String getMusic_time() {
        return music_time;
    }

    public void setMusic_time(String music_time) {
        this.music_time = music_time;
    }

    public String getMusic_singer() {
        return music_singer==null?"未知歌手":music_singer;
    }

    public void setMusic_singer(String music_singer) {
        this.music_singer = music_singer;
    }

    public String getMusic_album() {
        return music_album==null?"未知专辑":music_album;
    }

    public void setMusic_album(String music_album) {
        this.music_album = music_album;
    }

    public Bitmap getMusic_cover_map() {
        return music_cover_map;
    }

    public void setMusic_cover_map(Bitmap music_cover_map) {
        this.music_cover_map = music_cover_map;
    }

    public String getMusic_lyric() {
        return music_lyric;
    }

    public void setMusic_lyric(String music_lyric) {
        this.music_lyric = music_lyric;
    }

    public String getMusic_size() {
        return music_size;
    }

    public void setMusic_size(String music_size) {
        this.music_size = music_size;
    }

    public String getMusic_url() {
        return music_url;
    }

    public void setMusic_url(String music_url) {
        this.music_url = music_url;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(music_name);
        dest.writeString(music_time);
        dest.writeString(music_singer);
        dest.writeString(music_album);
        dest.writeParcelable(music_cover_map, flags);
        dest.writeString(music_lyric);
        dest.writeString(music_size);
        dest.writeString(music_url);
        dest.writeInt(time);
    }
}
