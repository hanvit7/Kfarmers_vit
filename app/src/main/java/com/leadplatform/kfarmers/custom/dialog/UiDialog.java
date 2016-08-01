package com.leadplatform.kfarmers.custom.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;

import com.leadplatform.kfarmers.R;

public class UiDialog {
	private static CustomDialogListener mCustomDialogListener = null;

	public static final int DIALOG_NEUTRAL_LISTENER = 0;
	public static final int DIALOG_POSITIVE_LISTENER = 1;
	public static final int DIALOG_NEGATIVE_LISTENER = 2;
	public static final int DIALOG_CANCEL_LISTENER = 3;

	public static void showDialog(Context context, String message) {
		mCustomDialogListener = null;
		createDialog(context, null, message, context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, String title, String message) {
		mCustomDialogListener = null;
		createDialog(context, title, message, context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, String message, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, null, message, context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, String title, String message, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, title, message, context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, int messageId) {
		mCustomDialogListener = null;
		createDialog(context, null, context.getString(messageId), context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, int messageId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, null, context.getString(messageId), context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, int titleId, int messageId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, context.getString(titleId), context.getString(messageId), context.getString(R.string.dialog_ok), null, null);
	}

	public static void showDialog(Context context, int messageId, int positiveId, int negativeId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, null, context.getString(messageId), null, context.getString(positiveId), context.getString(negativeId));
	}

	public static void showDialog(Context context, String message, int positiveId, int negativeId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, null, message, null, context.getString(positiveId), context.getString(negativeId));
	}

	public static void showDialog(Context context, String title, String message, int positiveId, int negativeId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, title, message, null, context.getString(positiveId), context.getString(negativeId));
	}
	
	public static void showDialog(Context context, String title, View view, int positiveId, int negativeId, CustomDialogListener listener) {
		mCustomDialogListener = listener;
		createDialog(context, title, view, null, context.getString(positiveId), context.getString(negativeId));
	}

    public static void showDialog(Context context, String title, View view, int positiveId, CustomDialogListener listener) {
        mCustomDialogListener = listener;
        createDialog(context, title, view, null, context.getString(positiveId), null);
    }
	
	private static void createDialog(Context context, String title, View view, String neturalText, String positiveText, String negativeText) {
		AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));

		if (title != null) {
			alert.setTitle(title);
		}

		if (view != null) {
			alert.setView(view);
		}

		if (neturalText != null) {
			alert.setNeutralButton(neturalText, mDefaultNeutralListener);
		}

		if (positiveText != null) {
			alert.setPositiveButton(positiveText, mDefaultPositiveListener);
		}

		if (negativeText != null) {
			alert.setNegativeButton(negativeText, mDefaultNegativeListener);
		}

		alert.setOnCancelListener(mDefaultCancelListener);
		alert.setOnKeyListener(mDefaultOnKeyListener);
		alert.setCancelable(false);

		if (!((Activity) context).isFinishing()) {
			alert.show();
		}
	}

	private static void createDialog(Context context, String title, String message, String neturalText, String positiveText, String negativeText) {
		AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));

		if (title != null) {
			alert.setTitle(title);
		}

		if (message != null) {
			alert.setMessage(message);
		}

		if (neturalText != null) {
			alert.setNeutralButton(neturalText, mDefaultNeutralListener);
		}

		if (positiveText != null) {
			alert.setPositiveButton(positiveText, mDefaultPositiveListener);
		}

		if (negativeText != null) {
			alert.setNegativeButton(negativeText, mDefaultNegativeListener);
		}

		alert.setOnCancelListener(mDefaultCancelListener);
		alert.setOnKeyListener(mDefaultOnKeyListener);
		alert.setCancelable(true);

		if (!((Activity) context).isFinishing()) {
			alert.show();
		}
	}

	private static DialogOnClickListener mDefaultNeutralListener = new DialogOnClickListener() {
		@Override
		public void dialogOnClick(DialogInterface dialog, int which) {
			if (mCustomDialogListener != null) {
				mCustomDialogListener.onDialog(DIALOG_NEUTRAL_LISTENER);
			}
		}
	};

	private static DialogOnClickListener mDefaultPositiveListener = new DialogOnClickListener() {
		@Override
		public void dialogOnClick(DialogInterface dialog, int which) {
			if (mCustomDialogListener != null) {
				mCustomDialogListener.onDialog(DIALOG_POSITIVE_LISTENER);
			}
		}
	};

	private static DialogOnClickListener mDefaultNegativeListener = new DialogOnClickListener() {
		@Override
		public void dialogOnClick(DialogInterface dialog, int which) {
			if (mCustomDialogListener != null) {
				mCustomDialogListener.onDialog(DIALOG_NEGATIVE_LISTENER);
			}
		}
	};

	private static DialogOnCancelListener mDefaultCancelListener = new DialogOnCancelListener() {
		@Override
		public void dialogOnCancel(DialogInterface dialog) {
			if (mCustomDialogListener != null) {
				mCustomDialogListener.onDialog(DIALOG_CANCEL_LISTENER);
			}
		}
	};

	private static DialogInterface.OnKeyListener mDefaultOnKeyListener = new DialogInterface.OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if ((keyCode == KeyEvent.KEYCODE_SEARCH) || (keyCode == KeyEvent.KEYCODE_MENU)) {
				return true;
			}
			return false;
		}
	};
}
