package com.leadplatform.kfarmers.view.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class RecipeProductFragment extends BaseFragment {
    public static final String TAG = "RecipeProductFragment";

    private LayoutInflater mInflater;

    ImageLoader imageLoader;
    DisplayImageOptions mOptions,mOptionsProfile;
    RecipeJson mRecipeJson;

    LinearLayout mProductLayout;


    public static RecipeProductFragment newInstance(RecipeJson data) {
        final RecipeProductFragment f = new RecipeProductFragment();
        final Bundle args = new Bundle();
        args.putSerializable("data", data);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecipeJson = (RecipeJson) getArguments().getSerializable("data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        mInflater = inflater;

        imageLoader = ((BaseFragmentActivity) getActivity()).imageLoader;
        mOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();
        mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 60) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();

        View v = inflater.inflate(R.layout.fragment_recipeproduct, container, false);

        mProductLayout = (LinearLayout) v.findViewById(R.id.productLayout);

        makeHeaderView();

        return v;
    }

    public void makeHeaderView()
    {
        for(int i=0 ; i< mRecipeJson.product.size(); i++) {
            View item = mInflater.inflate(R.layout.item_recipe_product_list, null);

            ImageView imageProduct = (ImageView) item.findViewById(R.id.image);
            ImageView imageProfile = (ImageView) item.findViewById(R.id.image_profile);

            TextView textProduct = (TextView) item.findViewById(R.id.product_name);
            //TextView textName = (TextView) item.findViewById(R.id.name);

            if(mRecipeJson.product.get(i).image1 != null && !mRecipeJson.product.get(i).image1.trim().isEmpty())
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mRecipeJson.product.get(i).image1,imageProduct,mOptions);
            if(mRecipeJson.product.get(i).profile_image != null && !mRecipeJson.product.get(i).profile_image.trim().isEmpty())
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + mRecipeJson.product.get(i).profile_image,imageProfile,mOptionsProfile);

            textProduct.setText(mRecipeJson.product.get(i).name);
            //textName.setText(mRecipeJson.product.get(i).farm_name);

            item.setTag(mRecipeJson.product.get(i).idx);

            item.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    Intent intent = new Intent(getSherlockActivity(), ProductActivity.class);
                    intent.putExtra("productIndex", (String) v.getTag());
                    startActivity(intent);
                }
            });

            mProductLayout.addView(item);
        }
    }
}
