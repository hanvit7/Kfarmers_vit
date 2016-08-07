package com.leadplatform.kfarmers.view.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class DialogFragment extends BaseDialogFragment {
    public static final String TAG = "DialogFragment";

    private int subMenuType;
    private int mSelect;
    private String mTitle;
    private String[] mList;
    private String fragmentTag;
    private OnCloseCategoryDialogListener listener;

    public static DialogFragment newInstance(
            int subMenuType,
            int selectIndex,
            String[] category,
            String fragmentTag) {
        final DialogFragment f = new DialogFragment();

        final Bundle args = new Bundle();
        args.putInt("SubMenuType", subMenuType);
        args.putInt("SelectIndex", selectIndex);
        args.putString("Title", "");
        args.putStringArray("Category", category);
        args.putString("FragmentTag", fragmentTag);
        f.setArguments(args);

        return f;
    }

    public static DialogFragment newInstance(
            int subMenuType,
            int selectIndex,
            String title,
            String[] category,
            String fragmentTag) {
        final DialogFragment f = new DialogFragment();

        final Bundle args = new Bundle();
        args.putInt("SubMenuType", subMenuType);
        args.putInt("SelectIndex", selectIndex);
        args.putString("Title", title);
        args.putStringArray("Category", category);
        args.putString("FragmentTag", fragmentTag);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);

        if (getArguments() != null) {
            subMenuType = getArguments().getInt("SubMenuType");
            mSelect = getArguments().getInt("SelectIndex");
            mTitle = getArguments().getString("Title");
            mList = getArguments().getStringArray("Category");
            fragmentTag = getArguments().getString("FragmentTag");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (TextUtils.isEmpty(mTitle))
            builder.setTitle(R.string.dialog_category_title);
        else
            builder.setTitle(mTitle + " " + getString(R.string.dialog_category_title));

        builder.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dismiss();
            }
        });
        builder.setSingleChoiceItems(mList, mSelect, new SingleChoiceListener());

        return builder.create();
    }

    private class SingleChoiceListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int position) {
            if (!fragmentTag.isEmpty()) {
                listener = (OnCloseCategoryDialogListener) getFragmentManager().findFragmentByTag(fragmentTag);
            }

            if (listener != null) {
                listener.onDialogSelected(subMenuType, position);
            }

            dismiss();
        }
    }

    public void setOnCloseCategoryDialogListener(OnCloseCategoryDialogListener listener) {
        this.listener = listener;
    }

    public interface OnCloseCategoryDialogListener {
        public void onDialogSelected(int subMenuType, int position);
    }

}
