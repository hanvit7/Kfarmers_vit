package com.leadplatform.kfarmers.view.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class CategoryDialogFragment extends BaseDialogFragment {
    public static final String TAG = "CategoryDialogFragment";

    private int subMenuType;
    private int selectIndex;
    private String title;
    private String[] categoryList;
    private String fragmentTag;
    private OnCloseCategoryDialogListener listener;

    public static CategoryDialogFragment newInstance(int subMenuType, int selectIndex, String[] category, String fragmentTag) {
        final CategoryDialogFragment f = new CategoryDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("SubMenuType", subMenuType);
        args.putInt("SelectIndex", selectIndex);
        args.putString("Title", "");
        args.putStringArray("Category", category);
        args.putString("FragmentTag", fragmentTag);
        f.setArguments(args);

        return f;
    }

    public static CategoryDialogFragment newInstance(int subMenuType, int selectIndex, String title, String[] category, String fragmentTag) {
        final CategoryDialogFragment f = new CategoryDialogFragment();

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
            selectIndex = getArguments().getInt("SelectIndex");
            title = getArguments().getString("Title");
            categoryList = getArguments().getStringArray("Category");
            fragmentTag = getArguments().getString("FragmentTag");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (TextUtils.isEmpty(title))
            builder.setTitle(R.string.dialog_category_title);
        else
            builder.setTitle(title + " " + getString(R.string.dialog_category_title));

        builder.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dismiss();
            }
        });
        builder.setSingleChoiceItems(categoryList, selectIndex, new SingleChoiceListener());

        return builder.create();
    }

    private class SingleChoiceListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int position) {
            if (!fragmentTag.isEmpty()) {
                listener = (OnCloseCategoryDialogListener) getFragmentManager().findFragmentByTag(fragmentTag);
            }

            if (listener != null) {
                listener.onDialogListSelection(subMenuType, position);
            }

            dismiss();
        }
    }

    public void setOnCloseCategoryDialogListener(OnCloseCategoryDialogListener listener) {
        this.listener = listener;
    }

    public interface OnCloseCategoryDialogListener {
        public void onDialogListSelection(int subMenuType, int position);
    }

}
