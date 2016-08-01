package com.leadplatform.kfarmers.controller;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.custom.toast.UiToast;
import com.leadplatform.kfarmers.util.CommonUtil;

public class UiController {
	public static void showProgressDialog(Context context) {
		if (context != null) {
			RelativeLayout progress = (RelativeLayout) ((SherlockFragmentActivity) context).findViewById(R.id.progress);
			if (progress != null) {
				hideSoftKeyboard(context);
				progress.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public static void hideProgressDialog(Context context) {
		if (context != null) {
			RelativeLayout progress = (RelativeLayout) ((SherlockFragmentActivity) context).findViewById(R.id.progress);
			if (progress != null) {
				hideSoftKeyboard(context);
				progress.setVisibility(View.GONE);
			}
		}
	}

    public static void showProgressDialogFragment(View v) {
        if (v != null) {
            RelativeLayout progress = (RelativeLayout) v.findViewById(R.id.progress);
            if (progress != null) {
                progress.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void hideProgressDialogFragment(View v) {
        if (v != null) {
            RelativeLayout progress = (RelativeLayout) v.findViewById(R.id.progress);
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
        }
    }
	

	public static void hideSoftKeyboard(Context context) {
		if (context != null) {
			View view = ((SherlockFragmentActivity) context).findViewById(R.id.root_container);
			if (view != null) {
				CommonUtil.UiUtil.hideSoftKeyboard(context, view);
			}
		}
	}

	public static void showDialog(Context context, int messageId) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, messageId);
	}

	public static void showDialog(Context context, String message) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, message);
	}

	public static void showDialog(Context context, int message, CustomDialogListener listener) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, message, listener);
	}

	public static void showDialog(Context context, int messageId, int positiveId, int negativeId, CustomDialogListener listener) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, messageId, positiveId, negativeId, listener);
	}

	public static void showDialog(Context context, String message, int positiveId, int negativeId, CustomDialogListener listener) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, message, positiveId, negativeId, listener);
	}

	public static void showDialog(Context context, String title, String message, int positiveId, int negativeId, CustomDialogListener listener) {
		hideSoftKeyboard(context);
		UiDialog.showDialog(context, title, message, positiveId, negativeId, listener);
	}

	public static void toastAddFavorite(Context context) {
		UiToast.show(context, R.drawable.favorite_add);
	}

	public static void toastCancelFavorite(Context context) {
		UiToast.show(context, R.drawable.favorite_cancel);
	}

	public static void toastAddLike(Context context) {
		UiToast.show(context, R.drawable.like_add);
	}

	public static void toastCancelLike(Context context) {
		UiToast.show(context, R.drawable.like_cancel);
	}

    public static void toastCartAdd(Context context) {
        UiToast.show(context, R.drawable.icon_cart_add);
    }
}
