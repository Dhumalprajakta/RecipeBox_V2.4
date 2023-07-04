package com.example.recipebox;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.ReviewAdapter;
import com.example.fragment.IngredientFragment;
import com.example.item.ItemLatest;
import com.example.item.ItemReview;
import com.example.util.API;
import com.example.util.Constant;
import com.example.util.FavClickListener;
import com.example.util.FavUnFavRecipe;
import com.example.util.JsonUtils;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class ActivityDetail extends AppCompatActivity {

    LinearLayout adLayout;
    ImageView imageView, img_share, image_d_fav;
    String Id;
    TextView text_view, text_recipe_name, text_time;
    FragmentManager fragmentManager;
    WebView webView_details;
    LinearLayout lyt_not_found;
    ProgressBar mProgressBar;
    ItemLatest objBean;
    ArrayList<String> mIngredient;
    ArrayList<ItemReview> mListReview;
    ReviewAdapter reviewAdapter;
    Menu menu;
    JsonUtils jsonUtils;
    String rateMsg;
    boolean iswhichscreen;
    MyApplication myApplication;
    TextView text_rate;
    LinearLayout lay_all_review;
    RatingView ratingView;
    TextView text_rate_total;
    CardView img_rate;
    LinearLayout sec_time;
    NestedScrollView lay_scroll;
    private YouTubePlayer youTubePlayer;
    public boolean isYouTubePlayerFullScreen;
    RelativeLayout sec_player_yt;
    String stringRateAvg, stringTotalRate, userRate, userRateMsg;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        objBean = new ItemLatest();
        mIngredient = new ArrayList<>();
        mListReview = new ArrayList<>();
        myApplication = MyApplication.getAppInstance();

        Intent i = getIntent();
        Id = i.getStringExtra("Id");

        adLayout = findViewById(R.id.ad_view);
        Intent intent2 = getIntent();
        iswhichscreen = intent2.getBooleanExtra("isNotification", false);
        if (!iswhichscreen) {
            if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
                JsonUtils.ShowBannerAds(ActivityDetail.this, adLayout);
            } else {
                JsonUtils.showNonPersonalizedAdsFB(adLayout, ActivityDetail.this);
            }
        }

        img_rate = findViewById(R.id.card_rate);
        text_view = findViewById(R.id.text_d_view);
        img_share = findViewById(R.id.image_d_share);
        text_recipe_name = findViewById(R.id.text_recipe_name);
        text_time = findViewById(R.id.text_time);
        fragmentManager = getSupportFragmentManager();
        webView_details = findViewById(R.id.webView_details);
        lyt_not_found = findViewById(R.id.lyt_not_found);
        mProgressBar = findViewById(R.id.progressBar);
        text_rate = findViewById(R.id.text_rate);
        image_d_fav = findViewById(R.id.image_d_fav);
        lay_all_review = findViewById(R.id.lay_all_review);
        ratingView = findViewById(R.id.ratingView);
        text_rate_total = findViewById(R.id.text_rate_total);
        sec_time = findViewById(R.id.sec_time);
        lay_scroll = findViewById(R.id.lay_scroll);
        imageView = findViewById(R.id.image_recipe);
        sec_player_yt = findViewById(R.id.sec_player);

        if (getResources().getString(R.string.isRTL).equals("true")) {
            sec_time.setBackgroundResource(R.drawable.time_detail_corner_rtl);
        } else {
            sec_time.setBackgroundResource(R.drawable.time_detail_corner);
        }
        lay_all_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListReview.clear();
                JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                jsObj.addProperty("method_name", "get_single_recipe");
                jsObj.addProperty("recipe_id", Id);
                jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
                if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                    new getDetailReview(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                }
            }
        });

        ratingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListReview.clear();
                JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                jsObj.addProperty("method_name", "get_single_recipe");
                jsObj.addProperty("recipe_id", Id);
                jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
                if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                    new getDetailReview(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                }
            }
        });

        text_rate_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListReview.clear();
                JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                jsObj.addProperty("method_name", "get_single_recipe");
                jsObj.addProperty("recipe_id", Id);
                jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
                if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                    new getDetailReview(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                }
            }
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_single_recipe");
        jsObj.addProperty("recipe_id", Id);
        jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
        if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
            new getDetail(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class getDetail extends AsyncTask<String, Void, String> {

        String base64;

        private getDetail(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showProgress(false);
            if (null == result || result.length() == 0) {
                lyt_not_found.setVisibility(View.VISIBLE);
            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        if (objJson.has("status")) {
                            lyt_not_found.setVisibility(View.VISIBLE);
                        } else {
                            objBean.setRecipeId(objJson.getString(Constant.LATEST_RECIPE_ID));
                            objBean.setRecipeName(objJson.getString(Constant.LATEST_RECIPE_NAME));
                            objBean.setRecipeType(objJson.getString(Constant.LATEST_RECIPE_TYPE));
                            objBean.setRecipeTime(objJson.getString(Constant.LATEST_RECIPE_TIME));
                            objBean.setRecipeIngredient(objJson.getString(Constant.LATEST_RECIPE_INGREDIENT));
                            objBean.setRecipeDirection(objJson.getString(Constant.LATEST_RECIPE_DIRE));
                            objBean.setRecipeImageBig(objJson.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                            objBean.setRecipePlayId(objJson.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                            objBean.setRecipeUrl(objJson.getString(Constant.LATEST_RECIPE_URL));
                            objBean.setRecipeViews(objJson.getString(Constant.LATEST_RECIPE_VIEW));
                            objBean.setRecipeAvgRate(objJson.getString(Constant.LATEST_RECIPE_AVR_RATE));
                            objBean.setRecipeTotalRate(objJson.getString(Constant.LATEST_RECIPE_TOTAL_RATE));
                            objBean.setRecipeCategoryName(objJson.getString(Constant.LATEST_RECIPE_CAT_NAME));
                            objBean.setFavourite(objJson.getBoolean(Constant.RECIPE_FAV));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.ARRAY_NAME_REVIEW);
                            if (jsonArrayChild.length() > 0 && !jsonArrayChild.get(0).equals("")) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemReview item = new ItemReview();
                                    item.setReviewName(objChild.getString(Constant.REVIEW_NAME));
                                    item.setReviewRate(objChild.getString(Constant.REVIEW_RATE));
                                    item.setReviewMessage(objChild.getString(Constant.REVIEW_MESSAGE));
                                    mListReview.add(item);
                                }
                            }
                            displayData();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void displayData() {

        text_view.setText(JsonUtils.Format(Integer.parseInt(objBean.getRecipeViews())) + " " + getString(R.string.view_title));
        text_recipe_name.setText(objBean.getRecipeName());
        text_time.setText(objBean.getRecipeTime());
        text_rate.setText(objBean.getRecipeAvgRate());
        WebSettings webSettings = webView_details.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView_details.setBackgroundColor(0);
        String text = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/myfonts/Montserrat-Regular.ttf\")}body,* {font-family: MyFont; color:#686868; font-size: 13px;line-height:1.2}img{max-width:100%;height:auto; border-radius: 3px;}</style>";
        webView_details.loadDataWithBaseURL("", text + "<div>" + objBean.getRecipeDirection() + "</div>", "text/html", "utf-8", null);

        Picasso.get().load(objBean.getRecipeImageBig()).placeholder(R.drawable.place_holder_big).into(imageView);
        ratingView.setRating(Float.parseFloat(objBean.getRecipeAvgRate()));
        if (objBean.getRecipeAvgRate().equals("0.0")) {
            text_rate_total.setVisibility(View.GONE);
        } else {
            text_rate_total.setText(objBean.getRecipeTotalRate() + " " + getString(R.string.rating_total_title));
        }


        if (objBean.getRecipeType().equals("video")) {
            imageView.setVisibility(View.GONE);
            sec_player_yt.setVisibility(View.VISIBLE);
            YouTubePlayerSupportFragmentX youTubePlayerFragment = YouTubePlayerSupportFragmentX.newInstance();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.Container, youTubePlayerFragment).commit();

            youTubePlayerFragment.initialize(getString(R.string.youtube_api_key), new YouTubePlayer.OnInitializedListener() {

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                    if (!wasRestored) {
                        youTubePlayer = player;
                        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        youTubePlayer.play();
                        player.cueVideo(objBean.getRecipePlayId());
                        youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {

                            @Override
                            public void onFullscreen(boolean _isFullScreen) {
                                isYouTubePlayerFullScreen = _isFullScreen;
                            }
                        });
                        player.cueVideo(objBean.getRecipePlayId());
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    String errorMessage = youTubeInitializationResult.toString();
                    Log.d("errorMessage:", errorMessage);
                }


            });
        } else {
            imageView.setVisibility(View.VISIBLE);
            sec_player_yt.setVisibility(View.GONE);
        }


        if (objBean.isFavourite()) {
            image_d_fav.setImageResource(R.drawable.fave_hov);
        } else {
            image_d_fav.setImageResource(R.drawable.fav_list);
        }
        image_d_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getAppInstance().getIsLogin()) {
                    if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                        FavClickListener saveClickListener = new FavClickListener() {
                            @Override
                            public void onItemClick(boolean isSave, String message) {
                                if (isSave) {
                                    image_d_fav.setImageResource(R.drawable.fave_hov);
                                } else {
                                    image_d_fav.setImageResource(R.drawable.fav_list);
                                }
                            }
                        };
                        new FavUnFavRecipe(ActivityDetail.this).userFav(objBean.getRecipeId(), saveClickListener);
                    } else {
                        Toast.makeText(ActivityDetail.this, getString(R.string.network_msg), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActivityDetail.this, getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                    Intent intentLogin = new Intent(ActivityDetail.this, SignInActivity.class);
                    intentLogin.putExtra("isfromdetail", true);
                    startActivity(intentLogin);
                }
            }
        });

        img_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.LATEST_RECIPE_IDD = objBean.getRecipeId();
                if (myApplication.getIsLogin()) {
                    JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                    jsObj.addProperty("method_name", "get_user_rating");
                    jsObj.addProperty("recipe_id", objBean.getRecipeId());
                    jsObj.addProperty("user_id", myApplication.getUserId());
                    if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                        new getRating(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                    }
                } else {
                    final PrettyDialog dialog = new PrettyDialog(ActivityDetail.this);
                    dialog.setTitle(getString(R.string.dialog_warning))
                            .setTitleColor(R.color.dialog_text)
                            .setMessage(getString(R.string.login_require))
                            .setMessageColor(R.color.dialog_text)
                            .setAnimationEnabled(false)
                            .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                @Override
                                public void onClick() {
                                    dialog.dismiss();
                                }
                            })
                            .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                @Override
                                public void onClick() {
                                    dialog.dismiss();
                                    Intent intent_login = new Intent(ActivityDetail.this, SignInActivity.class);
                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent_login.putExtra("isfromdetail", true);
                                    intent_login.putExtra("isid", Constant.LATEST_RECIPE_IDD);
                                    startActivity(intent_login);
                                }
                            })
                            .addButton(getString(R.string.dialog_no), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                @Override
                                public void onClick() {
                                    dialog.dismiss();
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new SaveTask(ActivityDetail.this)).execute(objBean.getRecipeImageBig());
            }
        });

        if (!objBean.getRecipeIngredient().isEmpty())
            mIngredient = new ArrayList<>(Arrays.asList(objBean.getRecipeIngredient().split(",")));

        if (!objBean.getRecipeIngredient().isEmpty()) {
            IngredientFragment ingredientFragment = IngredientFragment.newInstance(mIngredient);
            fragmentManager.beginTransaction().replace(R.id.ContainerIngredient, ingredientFragment).commit();
        }

    }


    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
            lay_scroll.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            lay_scroll.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getDetailReview extends AsyncTask<String, Void, String> {

        String base64;
        ProgressDialog pDialog;

        private getDetailReview(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ActivityDetail.this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        if (objJson.has("status")) {
                            lyt_not_found.setVisibility(View.VISIBLE);
                        } else {
                            objBean.setRecipeId(objJson.getString(Constant.LATEST_RECIPE_ID));
                            objBean.setRecipeName(objJson.getString(Constant.LATEST_RECIPE_NAME));
                            objBean.setRecipeType(objJson.getString(Constant.LATEST_RECIPE_TYPE));
                            objBean.setRecipeTime(objJson.getString(Constant.LATEST_RECIPE_TIME));
                            objBean.setRecipeIngredient(objJson.getString(Constant.LATEST_RECIPE_INGREDIENT));
                            objBean.setRecipeDirection(objJson.getString(Constant.LATEST_RECIPE_DIRE));
                            objBean.setRecipeImageBig(objJson.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                            objBean.setRecipePlayId(objJson.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                            objBean.setRecipeUrl(objJson.getString(Constant.LATEST_RECIPE_URL));
                            objBean.setRecipeViews(objJson.getString(Constant.LATEST_RECIPE_VIEW));
                            objBean.setRecipeAvgRate(objJson.getString(Constant.LATEST_RECIPE_AVR_RATE));
                            objBean.setRecipeTotalRate(objJson.getString(Constant.LATEST_RECIPE_TOTAL_RATE));
                            objBean.setRecipeCategoryName(objJson.getString(Constant.LATEST_RECIPE_CAT_NAME));
                            objBean.setFavourite(objJson.getBoolean(Constant.RECIPE_FAV));

                            JSONArray jsonArrayChild = objJson.getJSONArray(Constant.ARRAY_NAME_REVIEW);
                            if (jsonArrayChild.length() > 0 && !jsonArrayChild.get(0).equals("")) {
                                for (int j = 0; j < jsonArrayChild.length(); j++) {
                                    JSONObject objChild = jsonArrayChild.getJSONObject(j);
                                    ItemReview item = new ItemReview();
                                    item.setReviewName(objChild.getString(Constant.REVIEW_NAME));
                                    item.setReviewRate(objChild.getString(Constant.REVIEW_RATE));
                                    item.setReviewMessage(objChild.getString(Constant.REVIEW_MESSAGE));
                                    mListReview.add(item);
                                }
                            }
                            displayDataReview();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayDataReview() {
        showAllReview();
    }

    private void showAllReview() {
        final Dialog mDialog = new Dialog(ActivityDetail.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.review_all_dialog);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        RecyclerView recyclerView = mDialog.findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ActivityDetail.this, 1));
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
        TextView textView_no = mDialog.findViewById(R.id.no_fav);
        TextView text_dialog_review = mDialog.findViewById(R.id.text_dialog_review);
        ImageView image_close_dialog = mDialog.findViewById(R.id.image_close_dialog);

        text_dialog_review.setText(objBean.getRecipeAvgRate());
        image_close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        reviewAdapter = new ReviewAdapter(ActivityDetail.this, mListReview);
        recyclerView.setAdapter(reviewAdapter);

        if (reviewAdapter.getItemCount() == 0) {
            textView_no.setVisibility(View.VISIBLE);
        } else {
            textView_no.setVisibility(View.GONE);
        }
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class getRating extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;
        String base64;

        private getRating(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ActivityDetail.this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        userRate = objJson.getString("user_rate");
                        userRateMsg = objJson.getString("user_msg");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setgetRate();
            }
        }

        private void setgetRate() {
            showRateDialog();
        }
    }

    private void showRateDialog() {
        final String deviceId;
        final Dialog mDialog = new Dialog(ActivityDetail.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.rate_dialog);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        final RatingView ratingView = mDialog.findViewById(R.id.ratingView);
        ImageView image_rate_close = mDialog.findViewById(R.id.image_close);
        final EditText editTextReview = mDialog.findViewById(R.id.edt_d_review);
        ratingView.setRating(Float.parseFloat(userRate));
        editTextReview.setText(userRateMsg);
        Button button = mDialog.findViewById(R.id.btn_submit);

        image_rate_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextReview.getText().length() == 0) {
                    Toast.makeText(ActivityDetail.this, getString(R.string.require_review), Toast.LENGTH_SHORT).show();
                } else {
                    JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                    jsObj.addProperty("method_name", "recipe_rate");
                    jsObj.addProperty("device_id", deviceId);
                    jsObj.addProperty("recipe_id", objBean.getRecipeId());
                    jsObj.addProperty("user_id", myApplication.getUserId());
                    jsObj.addProperty("rate", ratingView.getRating());
                    jsObj.addProperty("message", editTextReview.getText().toString());
                    if (JsonUtils.isNetworkAvailable(ActivityDetail.this)) {
                        new sendRating(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                    }

                    mDialog.dismiss();
                }
            }
        });
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class sendRating extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;
        String Rate;
        String base64;

        private sendRating(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ActivityDetail.this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        rateMsg = objJson.getString("msg");
                        if (objJson.has(Constant.LATEST_RECIPE_AVR_RATE)) {
                            Rate = objJson.getString(Constant.LATEST_RECIPE_AVR_RATE);
                            stringRateAvg = objJson.getString("rate_avg");
                            stringTotalRate = objJson.getString("total_users");
                        } else {
                            Rate = "";
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setRate();
            }

        }

        private void setRate() {
            showToast(rateMsg);
            text_rate.setText(stringRateAvg);
            ratingView.setRating(Float.parseFloat(stringRateAvg));
            if (stringRateAvg.equals("0")) {
                text_rate_total.setVisibility(View.GONE);
            } else {
                text_rate_total.setVisibility(View.VISIBLE);
                text_rate_total.setText(stringTotalRate + " " + getString(R.string.rating_total_title));
            }

        }
    }

    public void showToast(String msg) {
        Toast.makeText(ActivityDetail.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isYouTubePlayerFullScreen) {
            youTubePlayer.setFullscreen(false);
        } else {
            if (!iswhichscreen) {
                super.onBackPressed();
            } else {
                Intent intent = new Intent(ActivityDetail.this, ActivityMain.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    public class SaveTask extends AsyncTask<String, String, String> {
        private Context context;
        URL myFileUrl;
        Bitmap bmImg = null;
        File file;
        public ProgressDialog pDialog;

        private SaveTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(context.getResources().getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            try {
                myFileUrl = new URL(args[0]);

                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                String filepath = getExternalCacheDir().getAbsolutePath();
                File dir = new File(filepath.toString());
                dir.mkdirs();
                String fileName = idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String args) {
            // TODO Auto-generated method stub
            pDialog.dismiss();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            if (objBean.getRecipeType().equals("video")) {
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_recipe_title) + objBean.getRecipeName() + "\n" + getString(R.string.share_recipe_ingredient) + Html.fromHtml(objBean.getRecipeIngredient()) + "\n" + getString(R.string.share_recipe_direction) + Html.fromHtml(objBean.getRecipeDirection()) + "\n" + getString(R.string.share_recipe_video) + "\n" + "https://www.youtube.com/watch?v=" + objBean.getRecipePlayId() + "\n" + getString(R.string.share_message) + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
            } else {
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_recipe_title) + objBean.getRecipeName() + "\n" + getString(R.string.share_recipe_ingredient) + Html.fromHtml(objBean.getRecipeIngredient()) + "\n" + getString(R.string.share_recipe_direction) + Html.fromHtml(objBean.getRecipeDirection()) + "\n" + getString(R.string.share_message) + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
            }
            Uri U = FileProvider.getUriForFile(ActivityDetail.this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            share.putExtra(Intent.EXTRA_STREAM, U);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                share.setClipData(ClipData.newRawUri("", U));
                share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }
}
