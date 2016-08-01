package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;

public class YoutubePlayActivity extends YouTubeBaseActivity  implements YouTubePlayer.OnInitializedListener
{

	private final static String TAG = "YoutubePlayActivity";
	private static final int RECOVERY_DIALOG_REQUEST = 1;

	private boolean isFullPlayer = false;
	private String idx = "";
	private ProductJson mProductJson;

	private YouTubePlayerView mYouTubePlayerView;
	private YouTubePlayer mYouTubePlayer;

	private View mView;

	private RelativeLayout mLayoutFarm,mLayoutProudct;

	private LinearLayout mLayoutBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*BlurBehind.getInstance()
				.withAlpha(255)
				.withFilterColor(Color.parseColor("#000000"))
				.setBackground(this);*/

		if(getIntent().hasExtra("idx") && !getIntent().getStringExtra("idx").trim().isEmpty())
		{
			idx = getIntent().getStringExtra("idx").trim();
		}
		else
		{
			finish();
		}

		if(getIntent().hasExtra("idx") && !getIntent().getStringExtra("idx").trim().isEmpty())
		{
			mProductJson = (ProductJson) getIntent().getSerializableExtra("data");
		}

		setContentView(R.layout.activity_youtube);

		mLayoutBtn = (LinearLayout) findViewById(R.id.LayoutBtn);
		mLayoutFarm = (RelativeLayout) findViewById(R.id.LayoutFarm);
		mLayoutProudct = (RelativeLayout) findViewById(R.id.LayoutProduct);
		mView = findViewById(R.id.root_container);

		mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube);
		mYouTubePlayerView.initialize(Constants.YOUTUBE_KEY, this);

		mView.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				finish();
			}
		});

		if(mProductJson != null) {
			mLayoutBtn.setVisibility(View.VISIBLE);
			mLayoutFarm.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {

					mYouTubePlayer.pause();
					Intent intent = new Intent(getApplicationContext(), FarmActivity.class);
					intent.putExtra("userType", "F");
					intent.putExtra("userIndex", mProductJson.member_idx);
					startActivity(intent);
					finish();
				}
			});

			mLayoutProudct.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {

					mYouTubePlayer.pause();
					Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
					intent.putExtra("productIndex", mProductJson.idx);
					startActivity(intent);
					finish();
				}
			});
		} else {
			mLayoutBtn.setVisibility(View.GONE);
		}
	}

	/*private  void makeProduct() {

		if(mProductJson == null) return;

		ImageLoader imageLoader = ImageLoader.getInstance();

		DisplayImageOptions optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).displayer(new FadeInBitmapDisplayer(1000, true, true, true))
				.build();

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(R.layout.item_recipe_product_list, null);

		ImageView imageProduct = (ImageView) item.findViewById(R.id.image);
		ImageView imageProfile = (ImageView) item.findViewById(R.id.image_profile);

		TextView textProduct = (TextView) item.findViewById(R.id.product_name);

		if(mProductJson.image1 != null && !mProductJson.image1.trim().isEmpty())
			imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mProductJson.image1,imageProduct,optionsProduct);


		textProduct.setText(mProductJson.name);
		//textName.setText(mRecipeJson.product.get(i).farm_name);

		item.setTag(mProductJson.idx);

		item.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
				intent.putExtra("productIndex", (String) v.getTag());
				startActivity(intent);
			}
		});

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LayoutProductList);
		linearLayout.addView(item);
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mYouTubePlayer != null)
		{
			mYouTubePlayer.release();
		}
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
		if (!b) {
			mYouTubePlayer = youTubePlayer;
			youTubePlayer.loadVideo(idx);
		}
	}

	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
		if (youTubeInitializationResult.isUserRecoverableError()) {
			youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
		} else {
			String errorMessage = youTubeInitializationResult.toString();
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RECOVERY_DIALOG_REQUEST) {
			mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube);
			mYouTubePlayerView.initialize(Constants.YOUTUBE_KEY, this);
		}
	}
}
