package com.example.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.LatestAdapter;
import com.example.item.ItemLatest;
import com.example.util.API;
import com.example.util.Constant;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.recipebox.MyApplication;
import com.example.recipebox.R;
import com.example.recipebox.SearchActivity;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LatestFragment extends Fragment {

    ArrayList<ItemLatest> mListItem;
    public RecyclerView recyclerView;
    LatestAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    int j = 1;
    MyApplication myApplication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getAppInstance();
        mListItem = new ArrayList<>();

        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_latest");
        jsObj.addProperty("user_id", myApplication.getUserId());
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getLatest(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }
        setHasOptionsMenu(true);
        return rootView;
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
        if (getActivity() != null) {
            adapter = new LatestAdapter(getActivity(), mListItem);
            recyclerView.setAdapter(adapter);

            if (adapter.getItemCount() == 0) {
                lyt_not_found.setVisibility(View.VISIBLE);
            } else {
                lyt_not_found.setVisibility(View.GONE);
            }
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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();

        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("search", query);
                startActivity(intent);
                searchView.clearFocus();
                return false;
            }
        });

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
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }
}
