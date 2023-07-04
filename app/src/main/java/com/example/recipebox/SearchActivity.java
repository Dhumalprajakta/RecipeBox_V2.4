package com.example.recipebox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.CategoryListAdapter;
import com.example.item.ItemLatest;
import com.example.util.API;
import com.example.util.Constant;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.recipebox.R;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class SearchActivity extends AppCompatActivity {

    ArrayList<ItemLatest> mListItem;
    public RecyclerView recyclerView;
    CategoryListAdapter categoryListAdapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    String search;
    LinearLayout adLayout;
    JsonUtils jsonUtils;
    int j = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        JsonUtils.setStatusBarGradiant(SearchActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        GlobalBus.getBus().register(this);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        Intent intent = getIntent();
        search = intent.getStringExtra("search");

        mListItem = new ArrayList<>();

        lyt_not_found = findViewById(R.id.lyt_not_found);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(SearchActivity.this, 1));
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
        adLayout = findViewById(R.id.adview);
        if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
            JsonUtils.ShowBannerAds(SearchActivity.this, adLayout);
        } else {
            JsonUtils.showNonPersonalizedAdsFB(adLayout, SearchActivity.this);
        }


        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_search_recipe");
        jsObj.addProperty("search_text", search);
        jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
        if (JsonUtils.isNetworkAvailable(SearchActivity.this)) {
            new getLatest(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class getLatest extends AsyncTask<String, Void, String> {

        String base64;

        private getLatest(String base64) {
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
                            ItemLatest objItem = new ItemLatest();
                            objItem.setRecipeId(objJson.getString(Constant.LATEST_RECIPE_ID));
                            objItem.setRecipeName(objJson.getString(Constant.LATEST_RECIPE_NAME));
                            objItem.setRecipeType(objJson.getString(Constant.LATEST_RECIPE_TYPE));
                            objItem.setRecipePlayId(objJson.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                            objItem.setRecipeImageSmall(objJson.getString(Constant.LATEST_RECIPE_IMAGE_SMALL));
                            objItem.setRecipeImageBig(objJson.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                            objItem.setRecipeViews(objJson.getString(Constant.LATEST_RECIPE_VIEW));
                            objItem.setRecipeTime(objJson.getString(Constant.LATEST_RECIPE_TIME));
                            objItem.setRecipeCategoryName(objJson.getString(Constant.LATEST_RECIPE_CAT_NAME));
                            objItem.setRecipeAvgRate(objJson.getString(Constant.LATEST_RECIPE_AVR_RATE));
                            objItem.setRecipeDirection(objJson.getString(Constant.LATEST_RECIPE_DIRE));
                            objItem.setRecipeIngredient(objJson.getString(Constant.LATEST_RECIPE_INGREDIENT));
                            objItem.setFavourite(objJson.getBoolean(Constant.RECIPE_FAV));

                            if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
                                if (j % Integer.parseInt(Constant.SAVE_NATIVE_CLICK_OTHER) == 0) {
                                    mListItem.add(null);
                                    j++;
                                }
                            }
                            mListItem.add(objItem);
                            j++;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }

    private void displayData() {

        categoryListAdapter = new CategoryListAdapter(SearchActivity.this, mListItem);
        recyclerView.setAdapter(categoryListAdapter);

        if (categoryListAdapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }

    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFavRecipe(Events.FavRecipe saveJob) {
        for (int i = 0; i < mListItem.size(); i++) {
            if (mListItem.get(i) != null) {
                if (mListItem.get(i).getRecipeId().equals(saveJob.getReId())) {
                    mListItem.get(i).setFavourite(saveJob.isFav());
                    categoryListAdapter.notifyItemChanged(i);
                }
            }
        }
    }
}
