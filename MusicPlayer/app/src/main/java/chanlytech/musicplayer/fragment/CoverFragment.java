package chanlytech.musicplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import chanlytech.musicplayer.R;
import chanlytech.musicplayer.activity.PlayActivity;
import chanlytech.musicplayer.view.CircleImageView;

/**
 * Created by Lyy on 2016/5/16.
 */
public class CoverFragment extends Fragment implements PlayActivity.MusicPlayStatusLinster {
    private View mView;
    private static CircleImageView mImageView;
//    private LRCTextView mLrcTextView;
//    private String basePath = MyApploctin.lrcPath;
    public CoverFragment() {

    }

    public static CoverFragment newInstance() {
        CoverFragment coverFragment = new CoverFragment();
        return coverFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayActivity.setMusicPlayStatuLinter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.cover_fragment, container, false);
        mImageView= (CircleImageView) mView.findViewById(R.id.iv_cover);
//        mLrcTextView= (LRCTextView) mView.findViewById(R.id.lrc);
//        File file = new File(basePath + "/" +"不说再见_好妹妹乐队.lrc");
//        if (file.exists()) {
//            try {
//                mLrcTextView.setLrcPath(file.getPath());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        mImageView.roatateStart();
        return mView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageView.destroyRoatate();
    }

    public static void creat(){
        if(mImageView!=null){
            Animation alphaAnimation=new AlphaAnimation(0.8f,0.0f);
            alphaAnimation.setDuration(1500);
            Animation mTranslateAnimation=new TranslateAnimation(0.0f,0.0f,0.0f,-800f);
            mTranslateAnimation.setDuration(1500);
            AnimationSet animationSet=new AnimationSet(false);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setFillAfter(true);
            animationSet.addAnimation(mTranslateAnimation);
            animationSet.setFillAfter(false);
            mImageView.startAnimation(animationSet);
        }

    }


    @Override
    public void isPlaying(boolean isplaying) {
        if(isplaying){
            mImageView.roatateStart();
        }else {
            mImageView.roatatePause();

        }
    }
}
