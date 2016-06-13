package chanlytech.musicplayer.entity;


import java.util.List;

/**
 * Created by Lyy on 2016/5/27.
 */
public class BaseData {
    private List<Onlinelyrics> song;
    private String order;
    private int error_code;
    private String album;
    private List<Data>data;
    public List<Onlinelyrics> getSong() {
        return song;
    }

    public void setSong(List<Onlinelyrics> song) {
        this.song = song;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
}
