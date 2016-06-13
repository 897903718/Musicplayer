package chanlytech.musicplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import chanlytech.musicplayer.R;
import chanlytech.musicplayer.entity.MusicEntity;

/**
 * Created by Lyy on 2016/5/16.
 */
public class MusicAdapter extends BaseAdapter {
    private Context mContext;
    private List<MusicEntity>mUsicEntities;
    public MusicAdapter(Context context,List<MusicEntity> musicEntities){
        this.mContext=context;
        this.mUsicEntities=musicEntities;
    }
    @Override
    public int getCount() {
        return mUsicEntities.size();
    }

    @Override
    public MusicEntity getItem(int position) {
        return mUsicEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder=null;
        if(convertView==null){
            mViewHolder=new ViewHolder();
            convertView=View.inflate(mContext, R.layout.music_item,null);
//            mViewHolder.mImageView= (ImageView) convertView.findViewById(R.id.image);
            mViewHolder.music_name= (TextView) convertView.findViewById(R.id.music_name);
            mViewHolder.music_play= (TextView) convertView.findViewById(R.id.music_play);
            mViewHolder.music_time= (TextView) convertView.findViewById(R.id.music_time);
            convertView.setTag(mViewHolder);
        }else {
            mViewHolder= (ViewHolder) convertView.getTag();
        }
        mViewHolder.music_name.setText(getItem(position).getMusic_name());
        mViewHolder.music_play.setText(getItem(position).getMusic_singer()+" | "+getItem(position).getMusic_album());
        mViewHolder.music_time.setText("时间："+getItem(position).getMusic_time());
//        mViewHolder.mImageView.setImageBitmap(getItem(position).getMusic_cover_map());
        return convertView;
    }


    class ViewHolder{
        ImageView mImageView;
        TextView music_name;
        TextView music_play;
        TextView music_time;
    }
}
