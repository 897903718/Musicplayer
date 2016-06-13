package chanlytech.musicplayer.entity;

import java.util.List;

/**
 * Created by Lyy on 2016/5/27.
 */
public class Data {
    private List<Onlinelyrics> songList;
    private String xcode;
    private int code;
    private LyricData data;
    public List<Onlinelyrics> getSongList() {
        return songList;
    }

    public void setSongList(List<Onlinelyrics> songList) {
        this.songList = songList;
    }

    public String getXcode() {
        return xcode;
    }

    public void setXcode(String xcode) {
        this.xcode = xcode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LyricData getData() {
        return data;
    }

    public void setData(LyricData data) {
        this.data = data;
    }
}

