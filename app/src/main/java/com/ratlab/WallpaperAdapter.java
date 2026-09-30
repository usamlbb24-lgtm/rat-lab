package com.ratlab;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class WallpaperAdapter extends BaseAdapter {

    private final Context ctx;
    private final int[] icons = {
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_slideshow,
            android.R.drawable.ic_menu_crop,
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_menu_view,
            android.R.drawable.ic_menu_share,
            android.R.drawable.ic_menu_upload
    };

    public WallpaperAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return icons[position];
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
                    300));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(0xFF0F3460);
            iv.setPadding(20, 20, 20, 20);
            iv.setImageResource(icons[position]);
            iv.setColorFilter(0xFFE94560);
        } else {
            iv = (ImageView) convertView;
        }
        return iv;
    }
}
