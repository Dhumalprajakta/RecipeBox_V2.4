package com.example.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.item.ItemLatest;
import com.example.recipebox.MyApplication;
import com.example.recipebox.R;
import com.example.recipebox.SignInActivity;
import com.example.util.Constant;
import com.example.util.FavClickListener;
import com.example.util.FavUnFavRecipe;
import com.example.util.JsonUtils;
import com.example.util.PopUpAds;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.github.ornolfr.ratingview.RatingView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class LatestAdapter extends RecyclerView.Adapter {

    private Activity activity;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_Ad = 0;
    private ArrayList<ItemLatest> dataList;
    private String s_title, s_image, s_ing, s_dir, s_type, s_play_id;

    public LatestAdapter(Activity context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.activity = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.row_latest_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_Ad) {
            View view = LayoutInflater.from(activity).inflate(R.layout.admob_adapter, parent, false);
            return new AdOption(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;
            final ItemLatest singleItem = dataList.get(position);
            Picasso.get().load(singleItem.getRecipeImageBig()).placeholder(R.drawable.place_holder_big).into(viewHolder.image);
            viewHolder.txt_time.setText(singleItem.getRecipeTime());
            viewHolder.txt_recipe.setText(singleItem.getRecipeName());
            viewHolder.txt_view.setText(JsonUtils.Format(Integer.parseInt(singleItem.getRecipeViews())));
            viewHolder.ratingView.setRating(Float.parseFloat(singleItem.getRecipeAvgRate()));

            viewHolder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopUpAds.ShowInterstitialAds(activity, singleItem.getRecipeId());
                }
            });


            if (singleItem.isFavourite()) {
                viewHolder.image_fav.setImageResource(R.drawable.fave_hov);
            } else {
                viewHolder.image_fav.setImageResource(R.drawable.fav_list);
            }

            viewHolder.image_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (MyApplication.getAppInstance().getIsLogin()) {
                        if (JsonUtils.isNetworkAvailable(activity)) {
                            FavClickListener saveClickListener = new FavClickListener() {
                                @Override
                                public void onItemClick(boolean isSave, String message) {
                                    if (isSave) {
                                        viewHolder.image_fav.setImageResource(R.drawable.fave_hov);
                                    } else {
                                        viewHolder.image_fav.setImageResource(R.drawable.fav_list);
                                    }
                                }
                            };
                            new FavUnFavRecipe(activity).userFav(singleItem.getRecipeId(), saveClickListener);
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.network_msg), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                        Intent intentLogin = new Intent(activity, SignInActivity.class);
                        intentLogin.putExtra("isfromdetail", true);
                        activity.startActivity(intentLogin);
                    }
                }
            });
        } else if (holder.getItemViewType() == VIEW_TYPE_Ad) {

            final AdOption adOption = (AdOption) holder;
            if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
                if (Constant.SAVE_NATIVE_TYPE.equals("admob")) {

                    if (adOption.linearLayout.getChildCount() == 0) {

                        View view = activity.getLayoutInflater().inflate(R.layout.admob_ad, null, true);

                        final TemplateView templateView = view.findViewById(R.id.my_template);
                        if (templateView.getParent() != null) {
                            ((ViewGroup) templateView.getParent()).removeView(templateView); // <- fix
                        }
                        adOption.linearLayout.addView(templateView);
                        AdLoader adLoader = new AdLoader.Builder(activity, Constant.SAVE_NATIVE_ID)
                                .forNativeAd(nativeAd -> {
                                    NativeTemplateStyle styles = new
                                            NativeTemplateStyle.Builder()
                                            .build();

                                    templateView.setStyles(styles);
                                    templateView.setNativeAd(nativeAd);

                                })
                                .build();

                        AdRequest adRequest;
                        if (JsonUtils.personalization_ad) {
                            adRequest = new AdRequest.Builder()
                                    .build();
                        } else {
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");
                            adRequest = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                    .build();

                        }
                        adLoader.loadAd(adRequest);
                    }

                } else {
                    if (adOption.linearLayout.getChildCount() == 0) {

                        LayoutInflater inflater = LayoutInflater.from(activity);
                        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, adOption.linearLayout, false);

                        adOption.linearLayout.addView(adView);

                        // Add the AdOptionsView
                        final LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);

                        // Create native UI using the ad metadata.
                        final TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                        final MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                        final TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                        final TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                        final TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                        final Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                        final NativeAd nativeAd = new NativeAd(activity, Constant.SAVE_NATIVE_ID);
                        //AdSettings.addTestDevice("1035dc69-0d11-45c5-bfaf-8f7f7e76e42a");
                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {
                                Log.d("status_data", "MediaDownloaded" + " " + ad.toString());
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Toast.makeText(activity, adError.toString(), Toast.LENGTH_SHORT).show();
                                Log.d("status_data", "error" + " " + adError.toString());
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                // Race condition, load() called again before last ad was displayed
                                if (nativeAd == null || nativeAd != ad) {
                                    return;
                                }
                                // Inflate Native Ad into Container
                                Log.d("status_data", "on load" + " " + ad.toString());

                                NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
                                AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
                                adChoicesContainer.removeAllViews();
                                adChoicesContainer.addView(adOptionsView, 0);

                                // Set the Text.
                                nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                nativeAdBody.setText(nativeAd.getAdBodyText());
                                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                // Create a list of clickable views
                                List<View> clickableViews = new ArrayList<>();
                                clickableViews.add(nativeAdTitle);
                                clickableViews.add(nativeAdCallToAction);

                                // Register the Title and CTA button to listen for clicks.
                                nativeAd.registerViewForInteraction(
                                        adOption.linearLayout,
                                        nativeAdMedia,
                                        clickableViews);
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.d("status_data", "AdClicked" + " " + ad.toString());
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                Log.d("status_data", "Impression" + " " + ad.toString());
                            }

                        };

                        // Request an ad
                        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_Ad;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image, image_fav;
        private TextView txt_recipe, txt_view, txt_time;
        private LinearLayout lyt_parent;
        private RatingView ratingView;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_recipe);
            image_fav = itemView.findViewById(R.id.img_fav_list);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            txt_recipe = itemView.findViewById(R.id.text_recipe_name);
            txt_view = itemView.findViewById(R.id.text_view);
            txt_time = itemView.findViewById(R.id.text_time);
            ratingView = itemView.findViewById(R.id.ratingView);
        }
    }

    public class AdOption extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;

        public AdOption(View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.adView_admob);

        }
    }
}