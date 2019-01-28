package com.aaron.justlike.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaron.justlike.R;
import com.aaron.justlike.entity.Album;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Album> mAlbums;

    public CollectionAdapter(Context context, List<Album> albums) {
        mContext = context;
        mAlbums = albums;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_collection_recycler_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 取出 Activity 传过来的数据
        Album album = mAlbums.get(position);
        String title = album.getCollectionTitle();
        String total = album.getElementTotal() + " 图片";
        String path = album.getImagePath();

        // 设置标题信息
        ((ViewHolder) holder).imageTitle.setText(title);
        ((ViewHolder) holder).imageTotal.setText(total);

        // 加载集合的封面图
        RequestOptions options = new RequestOptions()
                .placeholder(R.color.colorGrey)
                .priority(Priority.HIGH);
        Glide.with(mContext)
                .load(path)
                .apply(options)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ColorMatrix matrix = new ColorMatrix();
                        matrix.setScale(0.7F, 0.7F, 0.7F, 1);
                        resource.setColorFilter(new ColorMatrixColorFilter(matrix));
                        ((ViewHolder) holder).itemImage.setImageDrawable(resource);
//                        AlphaAnimation aa = new AlphaAnimation(0.5F, 1);
//                        aa.setDuration(250);
//                        aa.setFillAfter(true);
//                        ((ViewHolder) holder).itemImage.startAnimation(aa);
                        return false;
                    }
                })
                .into(((ViewHolder) holder).itemImage);
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView itemImage;
        TextView imageTotal;
        TextView imageTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemImage = itemView.findViewById(R.id.collection_image);
            imageTotal = itemView.findViewById(R.id.collection_image_total);
            imageTitle = itemView.findViewById(R.id.collection_image_title);
        }
    }
}