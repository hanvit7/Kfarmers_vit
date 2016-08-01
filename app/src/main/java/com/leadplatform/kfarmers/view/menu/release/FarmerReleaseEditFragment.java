package com.leadplatform.kfarmers.view.menu.release;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.ReleaseFarmerJson;
import com.leadplatform.kfarmers.model.parcel.ReleaseFarmerData;
import com.leadplatform.kfarmers.model.parcel.ReleaseListData;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.DatePickerDialogFragment;
import com.leadplatform.kfarmers.view.common.DatePickerDialogFragment.OnCloseDatePickerDialogListener;

import java.util.Calendar;

public class FarmerReleaseEditFragment extends BaseFragment implements OnCloseDatePickerDialogListener
{
    public static final String TAG = "FarmerReleaseEditFragment";
    private final int DATE_PICKER_DIALOG_RELEASE_START = 0;
    private final int DATE_PICKER_DIALOG_RELEASE_END = 1;

    private ReleaseListData listData;
    private TextView title, releaseDateStart, releaseDateEnd;
    private EditText releaseNote;
    private CheckBox alwaysCheck, finishCheck;
    private ReleaseFarmerJson releaseFarmerData;

    public static FarmerReleaseEditFragment newInstance(ReleaseListData listData)
    {
        final FarmerReleaseEditFragment f = new FarmerReleaseEditFragment();

        final Bundle args = new Bundle();
        args.putParcelable(ReleaseListData.KEY, listData);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            listData = getArguments().getParcelable(ReleaseListData.KEY);
        }
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SHIP_MANAGE_EDIT, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_farmer_release_edit, container, false);

        title = (TextView) v.findViewById(R.id.title);
        releaseDateStart = (TextView) v.findViewById(R.id.releaseDateStart);
        releaseDateEnd = (TextView) v.findViewById(R.id.releaseDateEnd);
        alwaysCheck = (CheckBox) v.findViewById(R.id.alwaysCheck);
        finishCheck = (CheckBox) v.findViewById(R.id.finishCheck);
        releaseNote = (EditText) v.findViewById(R.id.releaseNote);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        title.setText(listData.getSubName());

        releaseDateStart.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onReleaseDateStartClicked();
            }
        });

        releaseDateEnd.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onReleaseDateEndClicked();
            }
        });

        alwaysCheck.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    releaseDateStart.setEnabled(false);
                    releaseDateEnd.setEnabled(false);
                }
                else
                {
                    if (!finishCheck.isChecked())
                    {
                        releaseDateStart.setEnabled(true);
                        releaseDateEnd.setEnabled(true);
                    }
                }
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SHIP_MANAGE_EDIT, "Click_Always", isChecked==true ? "true":"false");
            }
        });

        finishCheck.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    releaseDateStart.setEnabled(false);
                    releaseDateEnd.setEnabled(false);
                    releaseNote.setEnabled(false);
                }
                else
                {
                    if (!alwaysCheck.isChecked())
                    {
                        releaseDateStart.setEnabled(true);
                        releaseDateEnd.setEnabled(true);
                    }
                    releaseNote.setEnabled(true);
                }

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_SHIP_MANAGE_EDIT, "Click_Finish", isChecked==true ? "true":"false");
            }
        });

        if (releaseFarmerData == null)
        {
            getReleaseItem();
        }

        ((FarmerReleaseActivity) getSherlockActivity()).displayReleaseEditFarmerActionBar();
    }

    @Override
    public void onDetach()
    {
        ((FarmerReleaseActivity) getSherlockActivity()).displayReleaseListFarmerActionBar();
        super.onDetach();
    }

    public void onReleaseEditBtnClicked()
    {
        KfarmersAnalytics.onClick(KfarmersAnalytics.S_SHIP_MANAGE_EDIT, "Click_ActionBar-Edit", null);

        ReleaseFarmerData data = new ReleaseFarmerData();

        data.setCategory(listData.getSubIndex());
        data.setReleaseDateStart(releaseDateStart.getText().toString());
        data.setReleaseDateEnd(releaseDateEnd.getText().toString());
        data.setAlways(alwaysCheck.isChecked());
        data.setFinish(finishCheck.isChecked());
        data.setReleaseNote(releaseNote.getText().toString());

        CenterController.editReleaseFarmer(data, new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            ((FarmerReleaseActivity) getSherlockActivity()).finishReleaseEditFragment();
                            break;

                        case 1002:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_release);
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void getReleaseItem()
    {
        CenterController.getReleaseFarmer(listData.getSubIndex(), new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);
                            releaseFarmerData = (ReleaseFarmerJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ReleaseFarmerJson.class);

                            if (!PatternUtil.isEmpty(releaseFarmerData.ReleaseDateStart))
                            {
                                releaseDateStart.setText(releaseFarmerData.ReleaseDateStart);
                            }

                            if (!PatternUtil.isEmpty(releaseFarmerData.ReleaseDateEnd))
                            {
                                releaseDateEnd.setText(releaseFarmerData.ReleaseDateEnd);
                            }

                            if (!PatternUtil.isEmpty(releaseFarmerData.ReleaseNote))
                            {
                                releaseNote.setText(releaseFarmerData.ReleaseNote);
                            }

                            if (!PatternUtil.isEmpty(releaseFarmerData.Alway))
                            {
                                alwaysCheck.setChecked((releaseFarmerData.Alway.equals("Y")) ? true : false);
                            }

                            if (!PatternUtil.isEmpty(releaseFarmerData.Finish))
                            {
                                finishCheck.setChecked((releaseFarmerData.Finish.equals("Y")) ? true : false);
                            }
                            break;

                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    private void onReleaseDateStartClicked()
    {
        Calendar c;

        if (TextUtils.isEmpty(releaseDateStart.getText().toString()))
        {
            c = Calendar.getInstance();
        }
        else
        {
            c = CommonUtil.TimeUtil.simpleDateFormat(releaseDateStart.getText().toString());
        }

        Fragment fragment = DatePickerDialogFragment.newInstance(DATE_PICKER_DIALOG_RELEASE_START, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH), FarmerReleaseEditFragment.TAG);
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ((SherlockDialogFragment) fragment).show(ft, DatePickerDialogFragment.TAG);
    }

    private void onReleaseDateEndClicked()
    {
        Calendar c;

        if (TextUtils.isEmpty(releaseDateEnd.getText().toString()))
        {
            c = Calendar.getInstance();
        }
        else
        {
            c = CommonUtil.TimeUtil.simpleDateFormat(releaseDateEnd.getText().toString());
        }

        Fragment fragment = DatePickerDialogFragment.newInstance(DATE_PICKER_DIALOG_RELEASE_END, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH), FarmerReleaseEditFragment.TAG);
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        ((SherlockDialogFragment) fragment).show(ft, DatePickerDialogFragment.TAG);
    }

    @Override
    public void onDateSet(int index, int year, int month, int day)
    {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);

        if (index == DATE_PICKER_DIALOG_RELEASE_START)
        {
            releaseDateStart.setText(CommonUtil.TimeUtil.simpleDateFormat(c.getTime()));
        }
        else if (index == DATE_PICKER_DIALOG_RELEASE_END)
        {
            releaseDateEnd.setText(CommonUtil.TimeUtil.simpleDateFormat(c.getTime()));
        }
    }
}
