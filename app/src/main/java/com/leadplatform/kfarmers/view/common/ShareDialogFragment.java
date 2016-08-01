package com.leadplatform.kfarmers.view.common;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class ShareDialogFragment extends BaseDialogFragment
{
    public static final String TAG = "ShareDialogFragment";

    private String[] categoryList;
    private String object;
    private String fragmentTag;

    public static ShareDialogFragment newInstance(String[] category, String object, String fragmentTag)
    {
        final ShareDialogFragment f = new ShareDialogFragment();

        final Bundle args = new Bundle();
        args.putStringArray("Category", category);
        args.putString("Object", object);
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
            categoryList = getArguments().getStringArray("Category");
            object = getArguments().getString("Object");
            fragmentTag = getArguments().getString("FragmentTag");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_category_title);
        builder.setNegativeButton(R.string.dialog_cancel, new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                dismiss();
            }
        });

        ArrayList<String> arrayList = new ArrayList<String>();
        Collections.addAll(arrayList, categoryList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.select_dialog_item, arrayList);
        builder.setAdapter(adapter, new SingleChoiceListener());
        // builder.setSingleChoiceItems(categoryList, -1, new SingleChoiceListener());
        return builder.create();
    }

    private class SingleChoiceListener implements DialogInterface.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int position)
        {
            OnCloseShareDialogListener listener = (OnCloseShareDialogListener) getFragmentManager().findFragmentByTag(fragmentTag);
            if (listener != null)
                listener.onDialogListSelection(position, object);
            else
                ((OnCloseShareDialogListener)getSherlockActivity()).onDialogListSelection(position, object);

            dismiss();
        }
    }

    public interface OnCloseShareDialogListener
    {
        public void onDialogListSelection(int position, String object);
    }
}
