package chanlytech.musicplayer.entity;

/**
 * Created by Lyy on 2016/5/30.
 */
public class LyricData {
    private String errorCode;
    private Data data;
    private String lrc;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }
}
