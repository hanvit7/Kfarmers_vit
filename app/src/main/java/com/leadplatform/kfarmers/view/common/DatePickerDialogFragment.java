package com.leadplatform.kfarmers.view.common;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class DatePickerDialogFragment extends BaseDialogFragment implements DatePickerDialog.OnDateSetListener
{
    public static final String TAG = "DatePickerDialogFragment";

    private int index, year, month, day;
    private String fragmentTag;

    public static DatePickerDialogFragment newInstance(int index, int year, int month, int day, String fragmentTag)
    {
        final DatePickerDialogFragment f = new DatePickerDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("index", index);
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putString("FragmentTag", fragmentTag);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            index = getArguments().getInt("index");
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
            day = getArguments().getInt("day");
            fragmentTag = getArguments().getString("FragmentTag");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker arg0, int year, int month, int day)
    {        
        OnCloseDatePickerDialogListener listener = (OnCloseDatePickerDialogListener) getFragmentManager().findFragmentByTag(fragmentTag);
        listener.onDateSet(index, year, month, day);
    }

    public interface OnCloseDatePickerDialogListener
    {
        public void onDateSet(int index, int year, int month, int day);
    }
}
