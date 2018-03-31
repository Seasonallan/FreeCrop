package com.season.freecrop.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.season.freecrop.R;

import java.util.List;

/**
 * 裁剪 框
 * author：Create linmd on 17/3/2 16:05
 */
public class ImageCropAdapter extends RecyclerView.Adapter<ImageCropAdapter.CropViewHolder> {
    Context mContext;

    private List<Integer> mData;
    private List<Integer> mDataSel;
    public ImageCropAdapter(Context context, List<Integer> data, List<Integer> dataSel) {
        mContext = context;
        mData = data;
        mDataSel = dataSel;
    }

    private int position = -1;
    public void onItemClick(int position){
        this.position = position;
        notifyDataSetChanged();
    }

    @Override
    public CropViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CropViewHolder holder, final int position) {
        if (position == this.position){
            holder.mIvPic.setImageResource(mDataSel.get(position));
        }else{
            holder.mIvPic.setImageResource(mData.get(position));
        }
        holder.mIvPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clearPosition() {
        position = -1;
        notifyDataSetChanged();
    }

    public class CropViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvPic;

        public CropViewHolder(View itemView) {
            super(itemView);
            mIvPic = (ImageView) itemView.findViewById(R.id.iv_source);
        }

    }
}

