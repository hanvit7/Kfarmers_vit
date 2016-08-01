package com.leadplatform.kfarmers.view.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJsonCustom;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.IOException;
import java.io.InputStream;

public class RecipeContentFragment extends BaseFragment {
    public static final String TAG = "RecipeContentFragment";

    ImageLoader imageLoader;
    DisplayImageOptions mOptions;
    RelativeLayout mMainContentView;

    LinearLayout mSubContentView;

    RecipeJsonCustom mRecipeJsonCustom;

    TextView mTextSubNo,mTextSubDes;

    RelativeLayout mViewMaterial;
    TextView mTextMaterialTitle,mTextMaterialPeple;
    WebView mWebSubDes;

    LinearLayout mDesLayout;


    public static RecipeContentFragment newInstance(RecipeJsonCustom recipeJsonCustom) {
        final RecipeContentFragment f = new RecipeContentFragment();
        final Bundle args = new Bundle();
        args.putSerializable("data", recipeJsonCustom);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecipeJsonCustom = (RecipeJsonCustom) getArguments().getSerializable("data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        imageLoader = ((BaseFragmentActivity) getActivity()).imageLoader;
        mOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();

        View v = inflater.inflate(R.layout.fragment_recipecontent, container, false);

        mMainContentView = (RelativeLayout) v.findViewById(R.id.mainContentView);
        mSubContentView = (LinearLayout) v.findViewById(R.id.subContentView);

        mDesLayout = (LinearLayout) v.findViewById(R.id.desLayout);

        mWebSubDes = (WebView) v.findViewById(R.id.WebSubDes);
        mTextSubDes = (TextView) v.findViewById(R.id.textSubDes);
        mTextSubNo = (TextView) v.findViewById(R.id.textSubNo);

        mViewMaterial = (RelativeLayout) v.findViewById(R.id.viewMaterial);
        mTextMaterialTitle = (TextView) v.findViewById(R.id.textMaterialTitle);
        mTextMaterialPeple = (TextView) v.findViewById(R.id.textMaterialPeple);

        if (mRecipeJsonCustom.nType == RecipeJsonCustom.type.title) {
            makeTitle(v);
        } else if (mRecipeJsonCustom.nType == RecipeJsonCustom.type.material) {
            makeMaterial(v);
        } else {
            makeRecipe(v);
        }
        return v;
    }

    private void makeTitle(View v) {

        mMainContentView.setVisibility(View.VISIBLE);
        mSubContentView.setVisibility(View.GONE);

        TextView textTitle = (TextView) v.findViewById(R.id.textTitle);
        TextView textName = (TextView) v.findViewById(R.id.textName);
        TextView textDes = (TextView) v.findViewById(R.id.textDes);

        textTitle.setText(mRecipeJsonCustom.title);
        textName.setText(mRecipeJsonCustom.cooking);
        textDes.setText(mRecipeJsonCustom.explain);
    }

    private void makeMaterial(View v) {

        mMainContentView.setVisibility(View.GONE);
        mSubContentView.setVisibility(View.VISIBLE);
        mViewMaterial.setVisibility(View.VISIBLE);
        mDesLayout.setVisibility(View.GONE);
        mWebSubDes.setVisibility(View.VISIBLE);
        mWebSubDes.setBackgroundColor(0);

        mTextMaterialTitle.setText(mRecipeJsonCustom.cooking);
        mTextMaterialPeple.setText(mRecipeJsonCustom.number);

        //mWebSubDes.loadData(mRecipeJsonCustom.material, "text/html; charset=utf-8", "utf-8");

        try {
            InputStream is = getResources().getAssets().open("template.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            mWebSubDes.loadDataWithBaseURL("file:///android_asset/",
                    new String(buffer).replace("[CONTENT]", mRecipeJsonCustom.material.toString()),
                    "text/html", "UTF-8", null);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void makeRecipe(View v) {

        mMainContentView.setVisibility(View.GONE);
        mSubContentView.setVisibility(View.VISIBLE);
        mViewMaterial.setVisibility(View.GONE);
        mDesLayout.setVisibility(View.VISIBLE);

        ImageView image = (ImageView) v.findViewById(R.id.ImageView);
        imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mRecipeJsonCustom.path, image, mOptions);

        mTextSubDes.setText(mRecipeJsonCustom.content);
        mTextSubNo.setText(mRecipeJsonCustom.no+".");

        image.setTag(Constants.KFARMERS_SNIPE_IMG + mRecipeJsonCustom.path);
        image.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (v.getTag() != null) {
                    String url = (String) v.getTag();
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                    intent.putExtra("image", url);
                    getActivity().startActivity(intent);
                }
            }
        });

    }
}

