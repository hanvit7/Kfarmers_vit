package com.leadplatform.kfarmers.view.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.OnKeyBackPressedListener;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.market.ProductActivity;

public class YoutubePlayFragment extends BaseFragment implements OnKeyBackPressedListener{

	public static final String TAG = "YoutubePlayFragment";//ProductActivity에서 참조

	private static final int RECOVERY_DIALOG_REQUEST = 1;

	private String idx = "";
	private boolean isFullPlayer = false;

	private YouTubePlayerSupportFragment mYouTubePlayerFragment;
	private YouTubePlayer mYoutubePlayer;

	public static YoutubePlayFragment newInstance(String idx) {

		final YoutubePlayFragment f = new YoutubePlayFragment();
		final Bundle args = new Bundle();
		args.putString("idx", idx);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			idx = getArguments().getString("idx");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_youtube, container, false);

		initYoutubePlayer();
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((ProductActivity)activity).setOnKeyBackPressedListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mYoutubePlayer.release();
	}

	public void initYoutubePlayer()
	{
		// 유투브 플레이어 삽입
		FragmentManager fragmentManager = getFragmentManager();
		mYouTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
		mYouTubePlayerFragment.initialize(Constants.YOUTUBE_KEY, new YouTubePlayer.OnInitializedListener() {
			@Override
			public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

				youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
				youTubePlayer.setShowFullscreenButton(true);
				mYoutubePlayer = youTubePlayer;
				mYoutubePlayer.loadVideo(idx);

				mYoutubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                    @Override
                    public void onFullscreen(boolean b) {
                        isFullPlayer = b;
                    }
                });
			}
			@Override
			public void onInitializationFailure(YouTubePlayer.Provider provider,
												YouTubeInitializationResult errorReason) {
				if (errorReason.isUserRecoverableError()) {

					errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
				} else {
					String errorMessage = String.format(
							"There was an error initializing the YouTubePlayer (%1$s)",
							errorReason.toString());
					Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
				}
			}
		});
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtube_fragment, mYouTubePlayerFragment);
		fragmentTransaction.commit();
	}

	@Override
	public void onBack()
	{
		if (isFullPlayer) {
			mYoutubePlayer.setFullscreen(false);
			mYoutubePlayer.pause();
		} else {
			ProductActivity productActivity = (ProductActivity) getActivity();
			productActivity.setOnKeyBackPressedListener(null);
			productActivity.onBackPressed();
		}
	}
}
