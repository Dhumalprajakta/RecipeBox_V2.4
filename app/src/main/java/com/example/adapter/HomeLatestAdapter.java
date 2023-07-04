package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.item.ItemLatest;
import com.example.util.PopUpAds;
import com.squareup.picasso.Picasso;
import com.example.recipebox.R;

import java.util.ArrayList;


public class HomeLatestAdapter extends RecyclerView.Adapter<HomeLatestAdapter.ItemRowHolder> {

    private ArrayList<ItemLatest> dataList;
    private Context mContext;

    public HomeLatestAdapter(Context context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.mContext = context;
     }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_latest_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, final int position) {
        final ItemLatest singleItem = dataList.get(position);

        Picasso.get().load(singleItem.getRecipeImageBig()).placeholder(R.drawable.place_holder_small).into(holder.image);
        holder.text_title.setText(singleItem.getRecipeName());
        holder.text_time.setText(singleItem.getRecipeTime());
        if (mContext.getResources().getString(R.string.isRTL).equals("true")) {
            holder.image_arrow.setRotation(180);
        }

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpAds.ShowInterstitialAds(mContext,singleItem.getRecipeId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView image,image_arrow;
        private TextView text_time, text_title;
        private RelativeLayout lyt_parent;

        private ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            text_time = itemView.findViewById(R.id.textAvg);
            text_title = itemView.findViewById(R.id.text_title);
            image_arrow=itemView.findViewById(R.id.image_arrow);
        }
    }
}
