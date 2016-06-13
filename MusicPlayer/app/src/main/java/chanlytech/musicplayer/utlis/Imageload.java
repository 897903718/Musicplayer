package chanlytech.musicplayer.utlis;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by Lyy on 2015/7/14.
 * 加载网络图片工具类
 */
public class Imageload {
    private static ImageLoader mImageLoader;
    private static ImageLoaderConfiguration mImageLoaderConfiguration;//图片配置
    private static DisplayImageOptions mDisplayImageOptions;//显示图片配置，圆角，加载失败

    /**显示圆角的图片
     *@param context 上下文对象
     *@param Rounded 设置圆角的大小
     *@param imagepath 图片路径
     *@param mImageView
     * */
    public static void loadImageRound(Context context, int Rounded, String imagepath, final ImageView mImageView) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, "Music_Play/ImagCache");
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context).threadPoolSize(2)//线程池内加载的数量
                .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
////            .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//              .memoryCacheSize(2 * 1024 * 1024)//缓存内存大小设置
//              .discCacheSize(50 * 1024 * 1024)//缓存大小设置
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//缓存路径MD5加密
                .discCacheFileCount(100)//缓存文件数量
                .discCache(new UnlimitedDiscCache(cacheDir))//缓存路径
                .build();
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) {
            ImageLoader.getInstance().init(mImageLoaderConfiguration);
        }
        mDisplayImageOptions = new DisplayImageOptions.Builder()
//               .showImageOnFail(R.mipmap.ic_list_default)
                .bitmapConfig(Bitmap.Config.RGB_565)
//              .showStubImage(R.mipmap.ic_list_default)//加载中的图片
//              .cacheInMemory(true)                               //启用内存缓存
//              .cacheOnDisc(true)//是否缓存到sd卡中，启用缓存
                .displayer(new RoundedBitmapDisplayer(Rounded))//是否圆角显示
//              .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        mImageLoader.displayImage(imagepath, mImageView, mDisplayImageOptions, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String s, View view) {
                //开始加载
            }
            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                //加载失败
            }
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                //加载完成,按照图片比列来缩放图片
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                //加载取消的时候执行
            }
        });
    }

    //加载不是圆角的图片
    /**加载不是圆角的图片
     *@param context 上下文对象
     *@param imagpath 图片路径
     *@param mImageView
     * */
    public static void LoadImag(Context context, String imagpath, ImageView mImageView) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, "Music_Play/ImagCache");
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context).threadPoolSize(2)//线程池内加载的数量
                .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
////            .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//              .memoryCacheSize(2 * 1024 * 1024)//缓存内存大小设置
//              .discCacheSize(50 * 1024 * 1024)//缓存大小设置
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//缓存路径MD5加密
                .discCacheFileCount(100)//缓存文件数量
                .discCache(new UnlimitedDiscCache(cacheDir))//缓存路径
                .build();
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) {
            ImageLoader.getInstance().init(mImageLoaderConfiguration);
        }
        mDisplayImageOptions = new DisplayImageOptions.Builder()
//              .showImageOnFail(R.mipmap.ic_list_default)
                .bitmapConfig(Bitmap.Config.RGB_565)
//              .showStubImage(R.mipmap.ic_list_default)//加载中的图片
//              .cacheInMemory(true)                               //启用内存缓存
                .cacheOnDisc(true)//是否缓存到sd卡中，启用缓存
              .displayer(new RoundedBitmapDisplayer(5))//是否圆角显示
//                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        mImageLoader.displayImage(imagpath, mImageView, mDisplayImageOptions, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String s, View view) {
                //开始加载
            }
            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                //加载失败
            }
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                //加载完成,按照图片比列来缩放图片
            }
            @Override
            public void onLoadingCancelled(String s, View view) {
                //加载取消的时候执行
            }
        });
    }

    /**
     * 从drawable中异步加载本地图片
     *
     * @param imageId
     * @param imageView
     */
    public  static void displayFromDrawable(int imageId, ImageView imageView,Context context) {
        // String imageUri = "drawable://" + R.drawable.image; // from drawables
        // (only images, non-9patch)
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context).threadPoolSize(2)//线程池内加载的数量
//                .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
////            .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//              .memoryCacheSize(2 * 1024 * 1024)//缓存内存大小设置
//              .discCacheSize(50 * 1024 * 1024)//缓存大小设置
//                .discCacheFileNameGenerator(new Md5FileNameGenerator())//缓存路径MD5加密
//                .discCacheFileCount(100)//缓存文件数量
//                .discCache(new UnlimitedDiscCache(cacheDir))//缓存路径
                .build();

        mDisplayImageOptions = new DisplayImageOptions.Builder()
//              .showImageOnFail(R.mipmap.ic_list_default)
                .bitmapConfig(Bitmap.Config.RGB_565)
//              .showStubImage(R.mipmap.ic_list_default)//加载中的图片
//              .cacheInMemory(true)                               //启用内存缓存
//                .cacheOnDisc(true)//是否缓存到sd卡中，启用缓存
              .displayer(new RoundedBitmapDisplayer(360))//是否圆角显示
//                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        mImageLoader = ImageLoader.getInstance();
        if (!mImageLoader.isInited()) {
            ImageLoader.getInstance().init(mImageLoaderConfiguration);
        }
        mImageLoader.displayImage("drawable://" + imageId,
                imageView, mDisplayImageOptions);
    }

    /**
     * 从assets文件夹中异步加载图片
     *
     * @param imageName
     *            图片名称，带后缀的，例如
     * @param imageView
     */
    public void dispalyFromAssets(String imageName, ImageView imageView) {
        // String imageUri = "assets://image.png"; // from assets
        ImageLoader.getInstance().displayImage("assets://" + imageName,
                imageView);
    }

    /**
     * 从内存卡中异步加载本地图片
     *
     * @param uri
     * @param imageView
     */
    public void displayFromSDCard(String uri, ImageView imageView) {
        // String imageUri = "file:///mnt/sdcard/image.png"; // from SD card
        ImageLoader.getInstance().displayImage("file://" + uri, imageView);
    }

    /**
     * 从内容提提供者中抓取图片
     */
    public void displayFromContent(String uri, ImageView imageView) {
        // String imageUri = "content://media/external/audio/albumart/13"; //
        // from content provider
        ImageLoader.getInstance().displayImage("content://" + uri, imageView);
    }


    /**
     * 清除缓存
     */
    public static void cleanCache() {
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.clearDiscCache();
        mImageLoader.clearMemoryCache();
    }
}
