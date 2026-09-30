package com.ratlab;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class WallpaperAdapter extends BaseAdapter {

    private final Context ctx;
    private final int[] images = {
            R.drawable.wp1,
            R.drawable.wp2,
            R.drawable.wp3,
            R.drawable.wp4,
            R.drawable.wp5,
            R.drawable.wp6,
            R.drawable.wp7,
            R.drawable.wp8,
            R.drawable.wp9,
            R.drawable.wp10,
            R.drawable.wp11
    };

    public WallpaperAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return images[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;
        if (convertView == null) {
            iv = new ImageView(ctx);
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    380));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(Color.parseColor("#16213E"));
            iv.setPadding(4, 4, 4, 4);
        } else {
            iv = (ImageView) convertView;
        }
        iv.setImageResource(images[position]);
        return iv;
    }
}
