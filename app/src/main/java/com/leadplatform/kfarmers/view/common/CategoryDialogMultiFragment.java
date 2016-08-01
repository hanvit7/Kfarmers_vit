package com.leadplatform.kfarmers.view.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class CategoryDialogMultiFragment extends BaseDialogFragment
{
    public static final String TAG = "CategoryDialogMultiFragment";

    private int subMenuType;
    private String title;
    private String[] categoryList;
    private boolean[] selectList;
    private String fragmentTag;
    private OnCloseCategoryMultiDialogListener listener;

    public static CategoryDialogMultiFragment newInstance(int subMenuType, boolean[] select, String[] category, String title, String fragmentTag)
    {
        final CategoryDialogMultiFragment f = new CategoryDialogMultiFragment();

        final Bundle args = new Bundle();
        args.putInt("SubMenuType", subMenuType);
        args.putString("Title", title);
        args.putStringArray("Category", category);
        args.putBooleanArray("Select", select);
        args.putString("FragmentTag", fragmentTag);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);

        if (getArguments() != null)
        {
            subMenuType = getArguments().getInt("SubMenuType");
            selectList = getArguments().getBooleanArray("Select");
            title = getArguments().getString("Title");
            categoryList = getArguments().getStringArray("Category");
            fragmentTag = getArguments().getString("FragmentTag");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (TextUtils.isEmpty(title))
            builder.setTitle(R.string.dialog_category_title);
        else
            builder.setTitle(title + " " + getString(R.string.dialog_category_title));

        builder.setNegativeButton(R.string.dialog_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                if (!fragmentTag.isEmpty()) {
                    listener = (OnCloseCategoryMultiDialogListener) getFragmentManager().findFragmentByTag(fragmentTag);
                }
                if (listener != null) {
                    listener.onDialogListMultiSelection(subMenuType, selectList);
                }
                dismiss();
            }
        });
        builder.setMultiChoiceItems(categoryList, selectList, new MultiChoiceListener());
        return builder.create();
    }

    private class MultiChoiceListener implements DialogInterface.OnMultiChoiceClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            selectList[which] = isChecked;
        }
    }

    public void setOnCloseCategoryMultiDialogListener(OnCloseCategoryMultiDialogListener listener)
    {
        this.listener = listener;
    }

    public interface OnCloseCategoryMultiDialogListener
    {
        public void onDialogListMultiSelection(int subMenuType, boolean[] select);
    }

}
