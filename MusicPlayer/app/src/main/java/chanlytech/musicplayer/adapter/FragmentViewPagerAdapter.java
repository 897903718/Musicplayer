package chanlytech.musicplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Lyy on 2016/5/3.
 */
public class FragmentViewPagerAdapter {

    private List<Fragment> fragments; // 每个Fragment对应一个Page
    private FragmentManager fragmentManager;
    private ViewPager viewPager; // viewPager对象
    private int currentPageIndex = 0; // 当前page索引（切换之前）
    private OnExtraPageChangeListener onExtraPageChangeListener; // ViewPager切换页面时的额外功能添加接口

    /**
     * 构造函数
     *
     * @param fragmentManager
     * @param viewPager
     * @param fragments
     */
    public FragmentViewPagerAdapter(FragmentManager fragmentManager,
                                    ViewPager viewPager, List<Fragment> fragments) {
        this.fragments = fragments;
        this.fragmentManager = fragmentManager;
        this.viewPager = viewPager;
        // PagerAdapter
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter();
        this.viewPager.setAdapter(myPagerAdapter);
        // PageChangeListener
        MyPageChangeListener myPageChangeListener = new MyPageChangeListener();
        this.viewPager.setOnPageChangeListener(myPageChangeListener);
    }

    /**
     * 当前page索引（切换之前）
     *
     * @return
     */
    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    /**
     * PagerAdapter 要使用PagerAdapter，首先要继承PagerAdapter类，然后至少覆盖以下方法
     * instantiateItem(ViewGroup,
     * int)【这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中】
     * destroyItem(ViewGroup, int, Object)【这个方法，是从ViewGroup中移出当前View】
     * getCount()【这个方法，是获取当前窗体界面数】 isViewFromObject(View, Object)
     * 【这个方法，在帮助文档中原文是could be implemented as return view ==
     * object,也就是用于判断是否由对象生成界面】
     *
     * @author sansung
     *
     */
    class MyPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = fragments.get(position);// 取得位置，获取出来Fragment
            if (!fragment.isAdded()) { // 如果fragment还没有added
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(fragment, fragment.getClass().getSimpleName());
                ft.commit();
                /**
                 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
                 * 会在进程的主线程中，用异步的方式来执行。 如果想要立即执行这个等待中的操作，就要调用这个方法（只能在主线程中调用）。
                 * 要注意的是，所有的回调和相关的行为都会在这个调用中被执行完成，因此要仔细确认这个方法的调用位置。
                 */
                fragmentManager.executePendingTransactions();
            }

            if (fragment.getView().getParent() == null) {
                container.addView(fragment.getView()); // 为viewpager增加布局
            }

            return fragment.getView();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(fragments.get(position).getView()); // 移出viewpager两边之外的page布局
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    /**
     * OnPageChangeListener
     *
     * @author sansung
     *
     */
    class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        /**
         * 此方法是在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2）。 arg0
         * ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
         */
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
                onExtraPageChangeListener.onExtraPageScrollStateChanged(arg0);
            }
        }

        /**
         * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。 其中三个参数的含义分别为： arg0
         * :当前页面，及你点击滑动的页面 arg1:当前页面偏移的百分比 arg2:当前页面偏移的像素位置
         */
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
                onExtraPageChangeListener.onExtraPageScrolled(arg0, arg1, arg2);
            }
        }

        /**
         * 此方法是页面跳转完后得到调用，arg0是你当前选中的页面的Position（位置编号）。
         */
        @Override
        public void onPageSelected(int arg0) {
            fragments.get(currentPageIndex).onPause(); // 调用切换前Fargment的onPause()
            // fragments.get(currentPageIndex).onStop(); //
            // 调用切换前Fargment的onStop()
            if (fragments.get(arg0).isAdded()) {
                // fragments.get(i).onStart(); // 调用切换后Fargment的onStart()
                fragments.get(arg0).onResume(); // 调用切换后Fargment的onResume()
            }

            currentPageIndex = arg0;

            if (null != onExtraPageChangeListener) { // 如果设置了额外功能接口
                onExtraPageChangeListener.onExtraPageSelected(arg0);
            }
        }
    }

    /**
     * page切换额外功能接口
     */
   public static class OnExtraPageChangeListener {
        public void onExtraPageScrolled(int i, float v, int i2) {
        }

        public void onExtraPageSelected(int i) {
        }

        public void onExtraPageScrollStateChanged(int i) {
        }
    }

    /**
     * 得到页面切换额外功能监听器
     *
     * @return onExtraPageChangeListener
     */
    public OnExtraPageChangeListener getOnExtraPageChangeListener() {
        return onExtraPageChangeListener;
    }

    /**
     * 设置页面切换额外功能监听器
     *
     * @param onExtraPageChangeListener
     */
    public void setOnExtraPageChangeListener(
            OnExtraPageChangeListener onExtraPageChangeListener) {
        this.onExtraPageChangeListener = onExtraPageChangeListener;
    }
}
