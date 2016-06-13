package chanlytech.musicplayer.utlis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 */
public class ImageLoader {
    private static ImageLoader mInstance;
    /**
     * 图片缓存核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 线程池的线程数量，默认为1
     */
    private static final int DEAFULT_THREAD_COUNT = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTasksQueue;
    /**
     * 轮询的线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHander;

    /**
     * 运行在UI线程的handler，用于给ImageView设置图片
     */
    private Handler mUIHandler;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
     */
    private Semaphore mSemaphorePoolThreadHander = new Semaphore(0);

    /**
     * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mSemaphoreThreadPool;


    /**
     * 队列的调度方式
     *
     * @author zhy
     */
    public enum Type {
        FIFO, LIFO
    }

    private ImageLoader(int ThreadCount, Type type) {
        init(ThreadCount, type);
    }

    /**
     * 初始化
     */
    private void init(int threadCount, Type type) {
        //后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //通过线程池取出任务进行执行
                        mThreadPool.execute(getTasks());
                        try {
                            //当任务量==threadCount时，阻塞
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHander.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
        //获取应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        //初始化线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTasksQueue = new LinkedList<>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }
    public static ImageLoader getImageLoader() {
        if (mInstance == null) {
            //同步
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.FIFO);
                }
            }
        }
        return mInstance;
    }
    public static ImageLoader getImageLoader(int threadCount,Type type) {
        if (mInstance == null) {
            //同步
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount,type);
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据path为imageview设置图片
     */
    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //获取得到图片，为imagview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    //将path与getTag存储的路径进行比较
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }
        Bitmap bitmap = getBitmapFromLruCache(path);
        if (bitmap != null) {
            refreashBitmap(path, imageView, bitmap);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //1.首先获取图片需要显示的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //2.压缩图片
                    Bitmap bitmap = decodeSampledBitmapFromResource(path, imageSize.width, imageSize.height);
                    //3.加入缓存
                    addBitmapToLruCache(path, bitmap);
                    //回调
                    refreashBitmap(path, imageView, bitmap);
                    //执行完一个任务释放一个任务
                    mSemaphoreThreadPool.release();
                }
            });
        }
    }

    protected void refreashBitmap(String path, ImageView imageView, Bitmap bitmap) {
        ImgBeanHolder imgBeanHolder = new ImgBeanHolder();
        imgBeanHolder.bitmap = bitmap;
        imgBeanHolder.imageView = imageView;
        imgBeanHolder.path = path;
        Message message = Message.obtain();
        message.obj = imgBeanHolder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入缓存
     **/
    protected void addBitmapToLruCache(String path, Bitmap bitmap) {
        if (getBitmapFromLruCache(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     */
    private Bitmap decodeSampledBitmapFromResource(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//获取图片的宽和高但并不把图片加载到内存中
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        //使用获取到的inSampleSize解析图片
        options.inJustDecodeBounds = false;//加载图片到内存
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 根据需求图片的宽和高和实际图片的宽和高计算SampleSize
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reWidth, int reHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (width > reWidth || height > reHeight) {
            // 计算出实际宽度和目标宽度的比率
            int widthRadio = Math.round(width * 1.0f / reWidth);
            int heightRadio = Math.round(height * 1.0f / reHeight);
            inSampleSize = Math.max(widthRadio, heightRadio);//最大的压缩
        }
        return inSampleSize;
    }

    /***
     * 根据imagview获取适当的压缩的宽和高
     */
    protected ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        int width = imageView.getWidth();//获取imageview的实际宽度
        if (width <= 0) {
            width = params.width;//获取imageview在layout中声明的宽度
        }

        if (width <= 0) {
            width = getImageFieldValue(imageView,"mMaxWidth");//检查最大值
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;
        }


        int height = imageView.getHeight();//获取imageview的实际宽度
        if (height <= 0) {
            height = params.height;//获取imageview在layout中声明的宽度
        }

        if (height <= 0) {
            height = getImageFieldValue(imageView, "mMaxHeight");//检查最大值
        }
        if (width <= 0) {
            height = displayMetrics.heightPixels;
        }

        imageSize.width = width;
        imageSize.height = height;
        return imageSize;

    }

    /**
     * 通过反射获取imageview的某个属性值
     */
    private static int getImageFieldValue(Object obj, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue=field.getInt(obj);
            if(fieldValue> 0 && fieldValue < Integer.MAX_VALUE){
                value=fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private synchronized void addTask(Runnable runnable) {
        mTasksQueue.add(runnable);
        try {
            //请求
            if (mPoolThreadHander == null) {
                mSemaphorePoolThreadHander.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    private Runnable getTasks() {
        if (mType == Type.FIFO) {

            return mTasksQueue.removeFirst();
        } else if (mType == Type.LIFO) {

            return mTasksQueue.removeLast();
        }
        return null;
    }

    //根据path在缓存中获取bitmap
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private class ImageSize {
        int width;
        int height;
    }

    private class ImgBeanHolder {
        String path;
        Bitmap bitmap;
        ImageView imageView;
    }
}
