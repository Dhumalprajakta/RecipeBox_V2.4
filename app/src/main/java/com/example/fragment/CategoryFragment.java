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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.CategoryAdapter;
import com.example.item.ItemCategory;
import com.example.util.API;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.example.util.OnClick;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.example.recipebox.ActivityMain;
import com.example.recipebox.R;
import com.example.recipebox.SearchActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CategoryFragment extends Fragment {

    ArrayList<ItemCategory> mListItem;
    public RecyclerView recyclerView;
    CategoryAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    int j = 1;
    JsonUtils jsonUtils;
    OnClick onClick;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mListItem = new ArrayList<>();

        lyt_not_found = rootView.findViewById(R.id.lyt_not_found);
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == 0) {
                    return 2;
                }
                return 1;
            }
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "cat_list");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getLatest(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }


        onClick=new OnClick() {
            @Override
            public void position(int position, String type, String id, String title) {

                Bundle bundle = new Bundle();
                bundle.putString("name", title);
                bundle.putString("Id", id);

                FragmentManager fm = getFragmentManager();
                SubCategoryFragment subCategoryFragment = new SubCategoryFragment();
                subCategoryFragment.setArguments(bundle);
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(CategoryFragment.this);
                ft.add(R.id.fragment1, subCategoryFragment, title);
                ft.addToBackStack(title);
                ft.commit();
                ((ActivityMain) requireActivity()).setToolbarTitle(title);
            }
        };
        jsonUtils=new JsonUtils(requireActivity(),onClick);

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
                            ItemCategory objItem = new ItemCategory();
                            objItem.setCategoryId(objJson.getString(Constant.CATEGORY_ID));
                            objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                            objItem.setCategoryImageSmall(objJson.getString(Constant.CATEGORY_IMAGE_SMALL));
                            objItem.setCategoryImageBig(objJson.getString(Constant.CATEGORY_IMAGE_BIG));
                            objItem.setCategoryImageThumb(objJson.getString(Constant.CATEGORY_IMAGE_THUMB));
                            objItem.setCategoryImageIcon(objJson.getString(Constant.CATEGORY_IMAGE_ICON));

                            if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
                                if (j % Integer.parseInt(Constant.SAVE_NATIVE_CLICK_CAT) == 0) {
                                    mListItem.add( null);
                                    j++;
                                }
                            }
                            mListItem.add(objItem);
                            j++;

                        }
                    }
                    displayData();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayData() {
        if (getActivity() != null) {
            adapter = new CategoryAdapter(getActivity(), mListItem,onClick);
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

        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

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
}
