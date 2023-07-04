package com.example.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.example.adapter.HomeCategoryAdapter;
import com.example.adapter.HomeLatestAdapter;
import com.example.adapter.HomeMostAdapter;
import com.example.adapter.HomeSliderAdapter;
import com.example.item.ItemCategory;
import com.example.item.ItemLatest;
import com.example.item.ItemSlider;
import com.example.recipebox.ActivityMain;
import com.example.recipebox.MyApplication;
import com.example.recipebox.ProfileEditActivity;
import com.example.recipebox.SearchActivity;
import com.example.recipebox.SignInActivity;
import com.example.util.API;
import com.example.util.Constant;
import com.example.util.EnchantedViewPager;
import com.example.util.Events;
import com.example.util.GlobalBus;
import com.example.util.JsonUtils;
import com.example.util.OnClick;
import com.example.util.PopUpAds;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.example.recipebox.R;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class HomeFragment extends Fragment {

    ScrollView mScrollView;
    ProgressBar mProgressBar;
    ArrayList<ItemLatest> mSliderList;
    RecyclerView mCatView, mLatestView, mMostView, mSliderView;
    HomeLatestAdapter homeLatestAdapter;
    ArrayList<ItemLatest> mLatestList, mMostList;
    ArrayList<ItemCategory> mCatList;
    Button btnCat, btnLatest, btnMost;
    HomeMostAdapter homeMostAdapter;
    HomeCategoryAdapter homeCategoryAdapter;
    EditText edt_search;
    MyApplication MyApp;
    HomeSliderAdapter homeSliderAdapter;
    JsonUtils jsonUtils;
    OnClick onClick;
    ImageView image_1, image_2, image_3;
    MyApplication myApplication;
    ArrayList<ItemSlider> mSlider;
    EnchantedViewPager mViewPager;
    CustomViewPagerAdapter mAdapter;
    TextView txt_slider_home;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        GlobalBus.getBus().register(this);
        myApplication = MyApplication.getAppInstance();

        mScrollView = rootView.findViewById(R.id.scrollView);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mCatView = rootView.findViewById(R.id.rv_latest_cat);
        mLatestView = rootView.findViewById(R.id.rv_latest_recipe);
        btnCat = rootView.findViewById(R.id.btn_latest_cat);
        btnLatest = rootView.findViewById(R.id.btn_latest_recipe);
        mMostView = rootView.findViewById(R.id.rv_latest_recipe_popular);
        btnMost = rootView.findViewById(R.id.btn_latest_recipe_most);
        mSliderView = rootView.findViewById(R.id.rv_slider);
        image_1 = rootView.findViewById(R.id.image_1);
        image_2 = rootView.findViewById(R.id.image_2);
        image_3 = rootView.findViewById(R.id.image_3);
        txt_slider_home = rootView.findViewById(R.id.txt_slider_home);
        if (getResources().getString(R.string.isRTL).equals("true")) {
            image_1.setRotation(180);
            image_2.setRotation(180);
            image_3.setRotation(180);
        }

        MyApp = MyApplication.getAppInstance();

        mSliderList = new ArrayList<>();
        mLatestList = new ArrayList<>();
        mCatList = new ArrayList<>();
        mMostList = new ArrayList<>();
        mSlider = new ArrayList<>();

        mSliderView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_sl = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mSliderView.setLayoutManager(layoutManager_sl);
        mSliderView.setFocusable(false);
        mSliderView.setNestedScrollingEnabled(false);

        mCatView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mCatView.setLayoutManager(layoutManager);
        mCatView.setFocusable(false);
        mCatView.setNestedScrollingEnabled(false);

        mLatestView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_cat = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLatestView.setLayoutManager(layoutManager_cat);
        mLatestView.setFocusable(false);
        mLatestView.setNestedScrollingEnabled(false);

        mMostView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_most = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mMostView.setLayoutManager(layoutManager_most);
        mMostView.setFocusable(false);
        mMostView.setNestedScrollingEnabled(false);
        edt_search = rootView.findViewById(R.id.edt_search);
        mViewPager = rootView.findViewById(R.id.viewPager);
        mViewPager.useScale();
        mViewPager.removeAlpha();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_home_banner");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getSlider(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }


        onClick = new OnClick() {
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
                ft.hide(HomeFragment.this);
                ft.add(R.id.fragment1, subCategoryFragment, title);
                ft.addToBackStack(title);
                ft.commit();
                ((ActivityMain) requireActivity()).setToolbarTitle(title);
            }
        };
        jsonUtils = new JsonUtils(requireActivity(), onClick);

        btnCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityMain) requireActivity()).highLightNavigation(2);
                String categoryName = getString(R.string.home_category);
                FragmentManager fm = getFragmentManager();
                CategoryFragment f1 = new CategoryFragment();
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment1, f1, categoryName);
                ft.commit();
                ((ActivityMain) requireActivity()).setToolbarTitle(categoryName);
            }
        });

        btnLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityMain) requireActivity()).highLightNavigation(1);
                String categoryName = getString(R.string.home_latest);
                FragmentManager fm = getFragmentManager();
                LatestFragment f1 = new LatestFragment();
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment1, f1, categoryName);
                ft.commit();
                ((ActivityMain) requireActivity()).setToolbarTitle(categoryName);
            }
        });

        btnMost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ActivityMain) requireActivity()).highLightNavigation(3);
                String categoryName = getString(R.string.menu_most);
                FragmentManager fm = getFragmentManager();
                MostViewFragment f1 = new MostViewFragment();
                assert fm != null;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment1, f1, categoryName);
                ft.commit();
                ((ActivityMain) requireActivity()).setToolbarTitle(categoryName);
            }
        });

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //do something
                    String st_search = edt_search.getText().toString();
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    intent.putExtra("search", st_search);
                    startActivity(intent);
                    edt_search.getText().clear();
                }
                return false;
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class getSlider extends AsyncTask<String, Void, String> {

        String base64;

        private getSlider(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemSlider objItem = new ItemSlider();
                        objItem.setSliderImage(objJson.getString("external_image"));
                        objItem.setSliderLink(objJson.getString("external_link"));
                        objItem.setSliderRecId(objJson.getString("recipe_id"));
                        objItem.setSliderType(objJson.getString("recipe_type"));
                        mSlider.add(objItem);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResultSlider();
            }
        }
    }

    private void setResultSlider() {
        if (getActivity() != null) {
        if (mSlider.size() == 0) {
            mViewPager.setVisibility(View.GONE);
        } else {
            mViewPager.setVisibility(View.VISIBLE);
            mAdapter = new CustomViewPagerAdapter();
            mViewPager.setAdapter(mAdapter);
        }
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_home");
        jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new Home(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

    }

    private class CustomViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        private CustomViewPagerAdapter() {
            // TODO Auto-generated constructor stub
            inflater = requireActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mSlider.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View imageLayout = inflater.inflate(R.layout.row_home_slider_item, container, false);
            assert imageLayout != null;
            ImageView image = imageLayout.findViewById(R.id.image);
            LinearLayout lytParent = imageLayout.findViewById(R.id.rootLayout);

            Picasso.get().load(mSlider.get(position).getSliderImage()).placeholder(R.drawable.place_holder_big).into(image);
            imageLayout.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
            lytParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSlider.get(position).getSliderType().equals("Recipe")) {
                        PopUpAds.ShowInterstitialAds(requireActivity(), mSlider.get(position).getSliderRecId());
                    } else {
                        if (!mSlider.get(position).getSliderLink().isEmpty()) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mSlider.get(position).getSliderLink())));
                        }
                    }
                }
            });
            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Home extends AsyncTask<String, Void, String> {

        String base64;

        private Home(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject jsonArray = mainJson.getJSONObject(Constant.ARRAY_NAME);
                    JSONArray jsonSlider = jsonArray.getJSONArray(Constant.HOME_FEATURED_ARRAY);
                    JSONObject objJsonSlider;
                    for (int i = 0; i < jsonSlider.length(); i++) {
                        objJsonSlider = jsonSlider.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();
                        objItem.setRecipeId(objJsonSlider.getString(Constant.LATEST_RECIPE_ID));
                        objItem.setRecipeType(objJsonSlider.getString(Constant.LATEST_RECIPE_TYPE));
                        objItem.setRecipeCategoryName(objJsonSlider.getString(Constant.LATEST_RECIPE_CAT_NAME));
                        objItem.setRecipeName(objJsonSlider.getString(Constant.LATEST_RECIPE_NAME));
                        objItem.setRecipeImageBig(objJsonSlider.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                        objItem.setRecipeImageSmall(objJsonSlider.getString(Constant.LATEST_RECIPE_IMAGE_SMALL));
                        objItem.setRecipePlayId(objJsonSlider.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                        objItem.setFavourite(objJsonSlider.getBoolean(Constant.RECIPE_FAV));
                        mSliderList.add(objItem);
                    }
                    JSONArray jsonLatest = jsonArray.getJSONArray(Constant.HOME_LATEST_CAT);
                    JSONObject objJsonCat;
                    for (int k = 0; k < jsonLatest.length(); k++) {
                        objJsonCat = jsonLatest.getJSONObject(k);
                        ItemCategory objItem = new ItemCategory();
                        objItem.setCategoryId(objJsonCat.getString(Constant.CATEGORY_ID));
                        objItem.setCategoryName(objJsonCat.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryImageBig(objJsonCat.getString(Constant.CATEGORY_IMAGE_BIG));
                        objItem.setCategoryImageSmall(objJsonCat.getString(Constant.CATEGORY_IMAGE_SMALL));
                        mCatList.add(objItem);
                    }

                    JSONArray jsonPopular = jsonArray.getJSONArray(Constant.HOME_LATEST_ARRAY);
                    JSONObject objJson;
                    for (int l = 0; l < jsonPopular.length(); l++) {
                        objJson = jsonPopular.getJSONObject(l);
                        ItemLatest objItem = new ItemLatest();
                        objItem.setRecipeId(objJson.getString(Constant.LATEST_RECIPE_ID));
                        objItem.setRecipeName(objJson.getString(Constant.LATEST_RECIPE_NAME));
                        objItem.setRecipeType(objJson.getString(Constant.LATEST_RECIPE_TYPE));
                        objItem.setRecipePlayId(objJson.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                        objItem.setRecipeImageSmall(objJson.getString(Constant.LATEST_RECIPE_IMAGE_SMALL));
                        objItem.setRecipeImageBig(objJson.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                        objItem.setRecipeViews(objJson.getString(Constant.LATEST_RECIPE_VIEW));
                        objItem.setRecipeTime(objJson.getString(Constant.LATEST_RECIPE_TIME));
                        objItem.setRecipeAvgRate(objJson.getString(Constant.LATEST_RECIPE_AVR_RATE));
                        objItem.setRecipeCategoryName(objJson.getString(Constant.LATEST_RECIPE_CAT_NAME));
                        objItem.setFavourite(objJson.getBoolean(Constant.RECIPE_FAV));
                        mLatestList.add(objItem);
                    }

                    JSONArray jsonPopularMost = jsonArray.getJSONArray(Constant.HOME_MOST_ARRAY);
                    JSONObject objJsonMost;
                    for (int l = 0; l < jsonPopularMost.length(); l++) {
                        objJsonMost = jsonPopularMost.getJSONObject(l);
                        ItemLatest objItem = new ItemLatest();
                        objItem.setRecipeId(objJsonMost.getString(Constant.LATEST_RECIPE_ID));
                        objItem.setRecipeName(objJsonMost.getString(Constant.LATEST_RECIPE_NAME));
                        objItem.setRecipeType(objJsonMost.getString(Constant.LATEST_RECIPE_TYPE));
                        objItem.setRecipePlayId(objJsonMost.getString(Constant.LATEST_RECIPE_VIDEO_PLAY));
                        objItem.setRecipeImageSmall(objJsonMost.getString(Constant.LATEST_RECIPE_IMAGE_SMALL));
                        objItem.setRecipeImageBig(objJsonMost.getString(Constant.LATEST_RECIPE_IMAGE_BIG));
                        objItem.setRecipeViews(objJsonMost.getString(Constant.LATEST_RECIPE_VIEW));
                        objItem.setRecipeTime(objJsonMost.getString(Constant.LATEST_RECIPE_TIME));
                        objItem.setRecipeAvgRate(objJsonMost.getString(Constant.LATEST_RECIPE_AVR_RATE));
                        objItem.setRecipeCategoryName(objJsonMost.getString(Constant.LATEST_RECIPE_CAT_NAME));
                        objItem.setFavourite(objJsonMost.getBoolean(Constant.RECIPE_FAV));
                        objItem.setRecipeTotalRate(objJsonMost.getString(Constant.LATEST_RECIPE_TOTAL_RATE));
                        mMostList.add(objItem);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    private void setResult() {
        if (getActivity() != null) {

            if (mSliderList.size() == 0) {
                txt_slider_home.setVisibility(View.GONE);
            } else {
                txt_slider_home.setVisibility(View.VISIBLE);
            }

            homeSliderAdapter = new HomeSliderAdapter(getActivity(), mSliderList);
            mSliderView.setAdapter(homeSliderAdapter);

            homeLatestAdapter = new HomeLatestAdapter(getActivity(), mLatestList);
            mLatestView.setAdapter(homeLatestAdapter);

            homeMostAdapter = new HomeMostAdapter(getActivity(), mMostList);
            mMostView.setAdapter(homeMostAdapter);

            homeCategoryAdapter = new HomeCategoryAdapter(getActivity(), mCatList, onClick);
            mCatView.setAdapter(homeCategoryAdapter);

        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.menu_profile:
                if (MyApp.getIsLogin()) {
                    Intent intent_edit = new Intent(getActivity(), ProfileEditActivity.class);
                    startActivity(intent_edit);
                } else {
                    final PrettyDialog dialog = new PrettyDialog(requireActivity());
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
                                    Intent intent_login = new Intent(getActivity(), SignInActivity.class);
                                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        for (int i = 0; i < mSliderList.size(); i++) {
            if (mSliderList.get(i) != null) {
                if (mSliderList.get(i).getRecipeId().equals(saveJob.getReId())) {
                    mSliderList.get(i).setFavourite(saveJob.isFav());
                    homeSliderAdapter.notifyItemChanged(i);
                }
            }
        }

        for (int i = 0; i < mMostList.size(); i++) {
            if (mMostList.get(i) != null) {
                if (mMostList.get(i).getRecipeId().equals(saveJob.getReId())) {
                    mMostList.get(i).setFavourite(saveJob.isFav());
                    homeMostAdapter.notifyItemChanged(i);
                }
            }
        }
    }
}
