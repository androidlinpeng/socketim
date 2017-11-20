package com.websocketim.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.websocketim.R;
import com.websocketim.view.GlideCircleImage;
import com.websocketim.view.GlideRoundTransform;

/**
 * Created by Administrator on 2017/11/14.
 */

public class GlideLoader {

    public static void LoderAvatar(Context context,String url, ImageView view){
        LoderAvatar(context,url,view,0);
    }

    public static void LoderAvatar(Context context,String uri, ImageView view,int round){
        Glide.with(context)
                .load(uri)
                .transform(new GlideRoundTransform(context,round))
                .placeholder(R.drawable.ic_head_default)
                .error(R.drawable.ic_head_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderCircleAvatar(Context context,String url, ImageView view){
        Glide.with(context)
                .load(url)
                .transform(new GlideCircleImage(context))
                .placeholder(R.drawable.ic_account_person_white_big)
                .error(R.drawable.ic_account_person_white_big)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderCircleImage(Context context,String uri, ImageView view){
        Glide.with(context)
                .load(uri)
                .transform(new GlideCircleImage(context))
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static void LoderImage(Context context,String url, ImageView view){
        LoderImage(context,url,view,0);
    }

    public static void LoderImage(Context context,String uri, ImageView view,int round){
        Glide.with(context)
                .load(uri)
                .transform(new GlideRoundTransform(context,round))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }
}
