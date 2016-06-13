package chanlytech.musicplayer.entity;

import java.util.List;

/**
 * Created by Lyy on 2016/6/1.
 */
public class TianTianBaseData {
    private int code;
    private int rows;
    private int pages;
    private List<LyriceEntity>data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<LyriceEntity> getData() {
        return data;
    }

    public void setData(List<LyriceEntity> data) {
        this.data = data;
    }
}
