package chanlytech.musicplayer.utlis;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import chanlytech.musicplayer.R;
import chanlytech.musicplayer.entity.MusicEntity;

/**
 * Created by Lyy on 2016/5/16.
 * 音乐扫描工具类
 */
public class MusicScan {
    private static String TAG = "chanlytech.musicplayer.utlis.MusicScan";
    private static MusicScan musicLoader;
    private static ArrayList<MusicEntity> musicEntities = new ArrayList<>();
    private ContentResolver contentResolver;
    //Uri，指向external的database   
    private Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    //projection：选择的列; where：过滤条件; sortOrder：排序。  
    private String[] projection =
            {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE

            };
    //获取专辑封面的Uri MediaStore.Images.ImageColumns Uri.parse("content://media/external/audio/albumart");
    private static final Uri albumArtUri = Uri.parse(MediaStore.Images.ImageColumns.DATA);

    public static MusicScan initMusicScan(Context context) {
        if (musicLoader != null) {
            return musicLoader;
        } else {
            musicLoader = new MusicScan(context);
        }
        return musicLoader;
    }

    private MusicScan(Context context) {
        contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            Log.i(TAG, "cursor=null");
        } else if (!cursor.moveToFirst()) {
            Log.i(TAG, "   Music Loader cursor.moveToFirst() returns false.");
        } else {
            int display_name = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int size = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int album_id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int is_music = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            do {
                String displayname = cursor.getString(display_name);
                String Album = cursor.getString(album);
                long Id = cursor.getLong(id);
                String Duration = cursor.getString(duration);
                long Size = cursor.getInt(size);
                String Artist = cursor.getString(artist);
                String url = cursor.getString(data);
                long Album_id = cursor.getInt(album_id);
                int IS_MUSIC = cursor.getInt(is_music);
                StringBuffer sb = new StringBuffer();
                sb.append(displayname);
                sb.append("\n");
                sb.append(Album);
                sb.append("\n");
                sb.append(Id);
                sb.append("\n");
                sb.append(Duration);
                sb.append("\n");
                sb.append(Size);
                sb.append("\n");
                sb.append(Artist);
                sb.append("\n");
                sb.append(url);
                sb.append("\n");
                sb.append(Album_id);
                sb.append("\n");
                sb.append(IS_MUSIC);
                sb.append("\n");
                sb.append(Size / 1024 + "kb");
                sb.append("\n");
                Log.i(TAG, sb.toString());
                if (Size / 1024 > 800) {
                    MusicEntity musicEntity = new MusicEntity();
                    musicEntity.setId(Id + "");
                    musicEntity.setMusic_name(displayname);
                    musicEntity.setMusic_album(Album);
                    String time = formatTime(Long.parseLong(Duration));
//                    Bitmap bitmap = createAlbumArt(url);
                    musicEntity.setMusic_time(time);
                    musicEntity.setTime(Integer.parseInt(Duration));
                    musicEntity.setMusic_singer(Artist);
                    musicEntity.setMusic_size(Size + "");
                    musicEntity.setMusic_url(url);
//                    musicEntity.setMusic_cover_map(bitmap);
                    musicEntities.add(musicEntity);
                }

            } while (cursor.moveToNext());
            //关闭游标
            cursor.close();
        }
    }

    public static ArrayList<MusicEntity> getMusicEntities() {
        return musicEntities;
    }

    /**
     * 格式化时间，将毫秒转换为分:秒格式
     *
     * @param time
     * @return
     */
    private String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    /**
     * @Description 获取专辑封面
     * @param filePath 文件路径，like XXX/XXX/XX.mp3
     * @return 专辑封面bitmap
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public Bitmap createAlbumArt(final String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] embedPic = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic.length); //转换为图片
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return bitmap;
    }


    private Cursor getCursor(String filePath, Context context) {
        String path = null;
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (c.moveToFirst()) {
            do {
// 通过Cursor 获取路径，如果路径相同则break；
                System.out.println("////////" + filePath);
                path = c.getString(c
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                System.out.println("?????????" + path);
// 查找到相同的路径则返回，此时cursorPosition 便是指向路径所指向的Cursor 便可以返回了
                if (path.equals(filePath)) {
// System.out.println("audioPath = " + path);
// System.out.println("filePath = " + filePath);
// cursorPosition = c.getPosition();
                    break;
                }
            } while (c.moveToNext());
        }
// 这两个没有什么作用，调试的时候用
// String audioPath = c.getString(c
// .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
//
// System.out.println("audioPath = " + audioPath);
        return c;
    }

    private String getAlbumArt(int album_id, Context context) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = context.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    private Bitmap getImage(Context context,String path) {
        Cursor currentCursor = getCursor(path, context);
        int album_id = currentCursor.getInt(currentCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        String albumArt = getAlbumArt(album_id, context);
        Bitmap bm = null;
        if (albumArt == null) {
        } else {
            bm = BitmapFactory.decodeFile(albumArt);
        }

        return bm;
    }

}
