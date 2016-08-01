package com.leadplatform.kfarmers.view.recipe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJson;
import com.leadplatform.kfarmers.model.json.snipe.RecipeJsonCustom;
import com.leadplatform.kfarmers.util.Blur;
import com.leadplatform.kfarmers.util.ImageUtils;
import com.leadplatform.kfarmers.util.Installation;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.reply.NewReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;

public class RecipeViewActivity extends BaseFragmentActivity {
    public static final String TAG = "RecipeViewActivity";

    private static final String BLURRED_IMG_PATH = "blurred_image.png";

    private String mRecipe = "";

    private boolean isModeYori = false;

    private DisplayImageOptions mOptions;

    private ViewPager mPagerContent;
    private PagerAdapterContent mAdapterContent;

    private ArrayList<String> arrContent = new ArrayList<>();

    private ImageView ImageViewNomal,ImageViewBlur;

    private View dimView,dimFullView;

    private LinearLayout mTopLayout,mBottomLayout;

    private ImageView mImageLight,mImageBack;

    private float alpha;

    private int mSceenWidth = 0;

    private RecipeJson mRecipeJson;
    private ArrayList<RecipeJsonCustom> mRecipeJsonCustomArrayList;

    private boolean isProduct = false;
    private int mPageCount = 0;

    private LinearLayout mLikeLayout,mReplyLayout,mProductLayout;

    private TextView mTextLike,mTextReply,mTextProduct;

    private ImageView mImageLike;

    private ImageView mImgArrowLeft,mImgArrowRight;

    @Override
    public void initActionBar() {}

    @Override
    public void onCreateView(Bundle savedInstanceState) {

        setContentView(R.layout.activity_recipeview);

        if(getIntent().hasExtra("recipe")) {
            mRecipe = getIntent().getStringExtra("recipe");
            KfarmersAnalytics.onScreen(KfarmersAnalytics.S_RECIPE_DETAIL, getIntent().getStringExtra("cooking"));
        }

        mSceenWidth = ImageUtils.getScreenWidth(this);

        mLikeLayout = (LinearLayout) findViewById(R.id.like_layout);
        mReplyLayout = (LinearLayout) findViewById(R.id.reply_layout);
        mProductLayout = (LinearLayout) findViewById(R.id.product_layout);

        mTextLike = (TextView) findViewById(R.id.text_like);
        mTextReply = (TextView) findViewById(R.id.text_reply);
        mTextProduct = (TextView) findViewById(R.id.text_product);

        mImageLike = (ImageView) findViewById(R.id.image_like);

        mImgArrowLeft = (ImageView) findViewById(R.id.img_left);
        mImgArrowRight = (ImageView) findViewById(R.id.img_right);

        mLikeLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                SnipeApiController.setRecipeRecommend(mRecipe, getId(), new SnipeResponseListener(RecipeViewActivity.this) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        switch (Code) {
                            case 200:
                                try {
                                    JsonNode root = JsonUtil.parseTree(content);
                                    if(root.get("recommend_action").textValue().equals("add")) {
                                        mImageLike.setImageResource(R.drawable.like_on);
                                        UiController.toastAddLike(RecipeViewActivity.this);
                                    } else {
                                        mImageLike.setImageResource(R.drawable.like_off);
                                        UiController.toastCancelLike(RecipeViewActivity.this);
                                    }
                                    mTextLike.setText(" " + root.get("recommend_count").asText() + " ");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                UiController.showDialog(RecipeViewActivity.this, R.string.dialog_unknown_error);
                                break;
                        }
                    }
                });
            }
        });

        mImageBack = (ImageView) findViewById(R.id.back);
        mImageBack.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                finish();
            }
        });

        mImageLight = (ImageView) findViewById(R.id.light);
        mImageLight.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if(!isModeYori) {
                    isModeYori = true;
                    mImageLight.setImageResource(R.drawable.light_on);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    UiController.showDialog(RecipeViewActivity.this, "요리모드로 설정되어 화면이 꺼지지 않습니다.");
                } else {
                    isModeYori = false;
                    mImageLight.setImageResource(R.drawable.light);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    UiController.showDialog(RecipeViewActivity.this, "요리모드로가 해제되었습니다.");
                }
            }
        });

        View.OnClickListener onClickListener = new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if(isProduct) {
                    mPagerContent.setCurrentItem(mAdapterContent.getCount() - 1, false);
                    alpha = 1;
                    dimView.setAlpha(0);
                    dimFullView.setAlpha(1);
                    ImageViewBlur.setAlpha(1f);
                    mTopLayout.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                    mImgArrowRight.setVisibility(View.GONE);
                    mImgArrowLeft.setVisibility(View.VISIBLE);
                } else {
                    UiController.showDialog(mContext,"연관된 상품이 없습니다.");
                }
            }
        };

        mReplyLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(RecipeViewActivity.this, NewReplyActivity.class);
                intent.putExtra("idx",mRecipe);
                intent.putExtra("title",mRecipeJson.cooking);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_in_from_now);
            }
        });
        mProductLayout.setOnClickListener(onClickListener);


        ImageViewNomal = (ImageView) findViewById(R.id.normal_image);
        ImageViewBlur = (ImageView) findViewById(R.id.blurred_image);
        dimView = (View) findViewById(R.id.dimView);
        dimFullView = (View) findViewById(R.id.dimFullView);

        mTopLayout = (LinearLayout) findViewById(R.id.topLayout);
        mBottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);



        mOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();

        mPagerContent = (ViewPager) findViewById(R.id.PagerContent);
        //mPagerContent.setPageMargin(30);

        mAdapterContent = new PagerAdapterContent(getSupportFragmentManager());
        mPagerContent.setAdapter(mAdapterContent);


        mOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();


        mPagerContent.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    alpha = positionOffset;
                    if (alpha > 1) {
                        alpha = 1;
                    }
                    ImageViewBlur.setAlpha(alpha);
                    dimView.setAlpha(alpha);
                } else if (position == mAdapterContent.getCount() - 2) {
                    alpha = positionOffset;
                    if (alpha > 1) {
                        alpha = 1;
                    }
                    if(isProduct) dimFullView.setAlpha(alpha);
                    /*mTopLayout.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                    dimView.setAlpha(alpha);*/
                }

                Log.d("ididid",position+"");
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mPagerContent.getCurrentItem() == mAdapterContent.getCount() - 1) {
                    if(isProduct) {
                        mTopLayout.setVisibility(View.GONE);
                        mBottomLayout.setVisibility(View.GONE);
                        dimView.setAlpha(0);
                    }
                    mImgArrowRight.setVisibility(View.GONE);
                } else if (mPagerContent.getCurrentItem() == 0) {
                    mTopLayout.setVisibility(View.VISIBLE);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mImgArrowRight.setVisibility(View.VISIBLE);
                    mImgArrowLeft.setVisibility(View.GONE);
                    dimView.setAlpha(0);
                } else {
                    mTopLayout.setVisibility(View.VISIBLE);
                    mBottomLayout.setVisibility(View.VISIBLE);
                    mImgArrowRight.setVisibility(View.VISIBLE);
                    mImgArrowLeft.setVisibility(View.VISIBLE);
                    dimView.setAlpha(1);
                }
            }
        });

        getData();
    }


    private void updateView() {
        Bitmap bmpBlurred = BitmapFactory.decodeFile(getFilesDir() + BLURRED_IMG_PATH);

        if(bmpBlurred != null) {
            bmpBlurred = Bitmap.createScaledBitmap(bmpBlurred, mSceenWidth, (int) (bmpBlurred.getHeight()
                    * ((float) mSceenWidth) / (float) bmpBlurred.getWidth()), false);
            ImageViewBlur.setImageBitmap(bmpBlurred);
        }
    }

    private String getId() {
        UserDb mUserData = DbController.queryCurrentUser(this);
        String id = "";
        if(mUserData != null) {
            id = mUserData.getUserID();
        } else {
            id = Installation.id(mContext);
        }
        return id;
    }

    private void getData() {

        SnipeApiController.getRecipeDetail(mRecipe,getId(), new SnipeResponseListener(this) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    if (Code == 200) {

                        mRecipeJson = (RecipeJson) JsonUtil.jsonToObject(content,RecipeJson.class);
                        mRecipeJsonCustomArrayList = new ArrayList<RecipeJsonCustom>();

                        RecipeJsonCustom recipeJsonCustom = new RecipeJsonCustom();
                        recipeJsonCustom.nType = RecipeJsonCustom.type.title;
                        recipeJsonCustom.idx = mRecipeJson.idx;
                        recipeJsonCustom.title = mRecipeJson.title;
                        recipeJsonCustom.explain = mRecipeJson.explain;
                        recipeJsonCustom.cooking = mRecipeJson.cooking;
                        recipeJsonCustom.number = mRecipeJson.number;
                        recipeJsonCustom.material = mRecipeJson.material;
                        mRecipeJsonCustomArrayList.add(recipeJsonCustom);

                        if(mRecipeJson.material != null && !mRecipeJson.material.isEmpty()) {
                            recipeJsonCustom = new RecipeJsonCustom();
                            recipeJsonCustom.nType = RecipeJsonCustom.type.material;
                            recipeJsonCustom.idx = mRecipeJson.idx;
                            recipeJsonCustom.title = mRecipeJson.title;
                            recipeJsonCustom.explain = mRecipeJson.explain;
                            recipeJsonCustom.cooking = mRecipeJson.cooking;
                            recipeJsonCustom.number = mRecipeJson.number;
                            recipeJsonCustom.material = mRecipeJson.material;
                            mRecipeJsonCustomArrayList.add(recipeJsonCustom);
                        }

                        int i = 1;
                        for( RecipeJson.Recipe recipe : mRecipeJson.recipe) {
                            recipeJsonCustom = new RecipeJsonCustom();
                            recipeJsonCustom.no = String.valueOf(i);
                            recipeJsonCustom.path = recipe.path;
                            recipeJsonCustom.content = recipe.content;
                            mRecipeJsonCustomArrayList.add(recipeJsonCustom);
                            i++;
                        }

                        if(mRecipeJson.picture != null && !mRecipeJson.picture.trim().isEmpty()) {
                            imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mRecipeJson.picture, ImageViewNomal);

                            final File blurredImage = new File(getFilesDir() + BLURRED_IMG_PATH);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 2;
                                    Bitmap image = imageLoader.loadImageSync(Constants.KFARMERS_SNIPE_IMG + mRecipeJson.picture);

                                    if(image != null) {
                                        Bitmap newImg = Blur.fastblur(RecipeViewActivity.this, image, 15);
                                        ImageUtils.storeImage(newImg, blurredImage);
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                updateView();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }

                        if(mRecipeJson.product.size()>0) {
                            mPageCount = mRecipeJsonCustomArrayList.size() + 1;
                            isProduct = true;
                        } else {
                            mPageCount = mRecipeJsonCustomArrayList.size();
                            isProduct = false;
                        }

                        mAdapterContent.notifyDataSetChanged();

                        mTextLike.setText(" " + mRecipeJson.recommend_count + " ");
                        mTextReply.setText(" " + mRecipeJson.comment_count +" ");
                        mTextProduct.setText(" " + mRecipeJson.product.size()+" ");


                        if(mRecipeJson.recommend_used.equals("true")) {
                            mImageLike.setImageResource(R.drawable.like_on);
                        } else {
                            mImageLike.setImageResource(R.drawable.like_off);
                        }

                    } else {
                        UiController.showDialog(RecipeViewActivity.this, R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(RecipeViewActivity.this, R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }


    public class PagerAdapterContent extends FragmentStatePagerAdapter {


        public PagerAdapterContent(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {

            if(getCount()-1 == arg0 && isProduct) {
                return RecipeProductFragment.newInstance(mRecipeJson);
            } else {
                return RecipeContentFragment.newInstance(mRecipeJsonCustomArrayList.get(arg0));
            }
        }

        @Override
        public int getCount() {
            return mPageCount;
        }
    }
}