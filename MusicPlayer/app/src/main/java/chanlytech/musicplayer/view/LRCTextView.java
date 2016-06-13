package chanlytech.musicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lyy on 2016/6/8.
 */
public class LRCTextView extends RelativeLayout {
    private TextView tvDefault;
    private TextView tvSelect;
    private String lrc = "我是测试歌词 我是测试歌词 我是测试歌词";
    private List<String> mLrcs = new ArrayList<String>(); // 存放歌词
    private List<Long> mTimes = new ArrayList<Long>(); // 存放时间
    private int mCurrentLine = 0; // 当前行
    private long mNextTime = 0l; // 保存下一句开始的时间
    private int mRows=2;      // 默认显示2行
    private Scroller mScroller;
    private int mOffsetY;   // y上的偏移
    public LRCTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new LinearInterpolator());
        init();
    }

    public LRCTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context, new LinearInterpolator());
        init();
    }

    public LRCTextView(Context context) {
        super(context);
        mScroller = new Scroller(context, new LinearInterpolator());
        init();
    }

    private void init() {
        tvDefault = new TextView(getContext());
//        tvDefault.setGravity(CENTER_HORIZONTAL);
        tvDefault.setText(lrc);
        tvDefault.setTextColor(Color.parseColor("#726463"));
        tvDefault.setEllipsize(null);
        tvDefault.setSingleLine();
        tvDefault.setTextSize(16);

        tvSelect = new TextView(getContext());
//        tvSelect.setGravity(CENTER_HORIZONTAL);
        tvSelect.setTextColor(Color.parseColor("#39DF7C"));
        tvSelect.setText(lrc);
        tvSelect.setEllipsize(null);
        tvSelect.setSingleLine();
        tvSelect.setTextSize(16);
        addView(tvDefault);
        addView(tvSelect);
        tvSelect.setWidth(0);
    }



    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setPercent(percent);
                }
            }, 10);
        }
    }
    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();
            if(mScroller.isFinished()) {
                int cur = mScroller.getCurrX();
                mCurrentLine = cur <= 1 ? 0 : cur - 1;
                mOffsetY = 0;
            }

            postInvalidate();
        }
    }
    private float percent;

    /**
     * 设置颜色渐变百分比
     *
     * @param percent
     */
    public void setPercent(float percent) {
        this.percent = percent;
        setSelectWidth((int) (getSelectWidth() * percent));
    }

    private int getSelectWidth() {
        return tvDefault.getWidth();
    }

    private void setSelectWidth(int pixels) {
        if (pixels <= getSelectWidth()) {
            tvSelect.setWidth(pixels);
            String currentLrc=mLrcs.get(mCurrentLine);
            setLrc(currentLrc);
        }
    }

    /**
     * 设置歌词
     *
     * @param lrc
     */
    public void setLrc(String lrc) {
        this.lrc = lrc;
        tvDefault.setText(lrc);
        tvSelect.setText(lrc);
    }

    // 外部提供方法
    // 设置lrc的路径
    public void setLrcPath(String path) {
        reset();

        if (path == null) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            postInvalidate();
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            String line;
            List<LrcLine> lrcLinesPerInFileLine;
            List<LrcLine> allLines = new LinkedList<LrcLine>();

            while (null != (line = reader.readLine())) {
                lrcLinesPerInFileLine = parseLine(line);
                if (lrcLinesPerInFileLine == null) {
                    continue;
                }

                allLines.addAll(lrcLinesPerInFileLine);
            }
            Collections.sort(allLines);

            mTimes.clear();
            mLrcs.clear();
            if (allLines.isEmpty()) {
                return;
            }
            LrcLine lastLine = allLines.get(allLines.size() - 1);
            if (TextUtils.isEmpty(lastLine.line) || lastLine.line.trim().isEmpty()) {
                allLines.remove(allLines.size() - 1);
            }
            for (LrcLine l : allLines) {
                mTimes.add(l.time);
                mLrcs.add(l.line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 解析时间
    private Long parseTime(String time) {
        // 03:02.12
        String[] min = time.split(":");
        String[] sec = min[1].split("\\.");

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());

        return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
    }

    // 解析每行
    private List<LrcLine> parseLine(String line) {
        Matcher matcher = Pattern.compile("\\[\\d.+\\].+").matcher(line);
        // 如果形如：[xxx]后面啥也没有的，则return空
        if (!matcher.matches()) {
            return null;
        }

        line = line.replaceAll("\\[", "");
        if (line.endsWith("]")) {
            line += " ";
        }
        String[] result = line.split("\\]");
        int size = result.length;
        if (size == 0) {
            return null;
        }
        List<LrcLine> ret = new LinkedList<LrcLine>();
        if (size == 1) {
            LrcLine l = new LrcLine();
            l.time = parseTime(result[0]);
            l.line = "";
            ret.add(l);
        } else {
            for (int i = 0; i < size - 1; i++) {
                LrcLine l = new LrcLine();
                l.time = parseTime(result[i]);
                l.line = result[size - 1];
                ret.add(l);
            }
        }

        return ret;
    }


    private void reset() {
        mLrcs.clear();
        mTimes.clear();
        mCurrentLine = 0;
        mNextTime = 0l;
    }

    private static class LrcLine implements Comparable<LrcLine> {
        long time;
        String line;

        @Override
        public int compareTo(LrcLine another) {
            return (int) (time - another.time);
        }
    }
}
