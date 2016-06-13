package chanlytech.musicplayer.entity;

import java.util.List;

/**
 * Created by Lyy on 2016/6/1.
 */
public class LyriceEntity {
    private int vip;//是否是vip
    private String song_id;//歌曲id
    private String song_name;//歌曲名字
    private String singer_name;//歌手
    private String album_id;//专辑id
    private String album_name;//专辑名字
    private String pick_count;//选择数
    private List<Audition_list>audition_list;

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getSinger_name() {
        return singer_name;
    }

    public void setSinger_name(String singer_name) {
        this.singer_name = singer_name;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getPick_count() {
        return pick_count;
    }

    public void setPick_count(String pick_count) {
        this.pick_count = pick_count;
    }

    public List<Audition_list> getAudition_list() {
        return audition_list;
    }

    public void setAudition_list(List<Audition_list> audition_list) {
        this.audition_list = audition_list;
    }
}
