package com.example.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.item.ItemLatest;
import com.example.util.FavClickListener;
import com.example.util.FavUnFavRecipe;
import com.example.util.JsonUtils;
import com.example.util.PopUpAds;
import com.github.ornolfr.ratingview.RatingView;
import com.squareup.picasso.Picasso;
import com.example.recipebox.MyApplication;
import com.example.recipebox.R;
import com.example.recipebox.SignInActivity;

import java.util.ArrayList;


public class HomeMostAdapter extends RecyclerView.Adapter<HomeMostAdapter.ItemRowHolder> {

    private ArrayList<ItemLatest> dataList;
    private Activity mContext;

    public HomeMostAdapter(Activity context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, final int position) {
        final ItemLatest singleItem = dataList.get(position);

        Picasso.get().load(singleItem.getRecipeImageBig()).placeholder(R.drawable.place_holder_small).into(holder.image);
        holder.text_title.setText(singleItem.getRecipeName());
        holder.text_time.setText(singleItem.getRecipeTime());
        holder.textAvg.setText("(" + singleItem.getRecipeTotalRate() + ")");
        holder.ratingView.setRating(Float.parseFloat(singleItem.getRecipeAvgRate()));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpAds.ShowInterstitialAds(mContext,singleItem.getRecipeId());
            }
        });

        if (singleItem.isFavourite()) {
            holder.image_list_fav.setImageResource(R.drawable.fave_hov);
        } else {
            holder.image_list_fav.setImageResource(R.drawable.fav_list);
        }

        holder.image_list_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getAppInstance().getIsLogin()) {
                    if (JsonUtils.isNetworkAvailable(mContext)) {
                        FavClickListener saveClickListener = new FavClickListener() {
                            @Override
                            public void onItemClick(boolean isSave, String message) {
                                if (isSave) {
                                    holder.image_list_fav.setImageResource(R.drawable.fave_hov);
                                } else {
                                    holder.image_list_fav.setImageResource(R.drawable.fav_list);
                                }
                            }
                        };
                        new FavUnFavRecipe(mContext).userFav(singleItem.getRecipeId(),saveClickListener);
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.network_msg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                    Intent intentLogin = new Intent(mContext, SignInActivity.class);
                    intentLogin.putExtra("isfromdetail", true);
                    mContext.startActivity(intentLogin);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView image, image_list_fav;
        private TextView text_time, text_title, textAvg;
        private RelativeLayout lyt_parent;
        private RatingView ratingView;

        private ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            image_list_fav = itemView.findViewById(R.id.image_list_fav);
            text_time = itemView.findViewById(R.id.text_time);
            text_title = itemView.findViewById(R.id.text_title);
            textAvg = itemView.findViewById(R.id.textAvg);
            ratingView = itemView.findViewById(R.id.ratingView);
        }
    }
}
