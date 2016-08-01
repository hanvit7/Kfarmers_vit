package com.leadplatform.kfarmers.view.sns;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;

public class SnsBlogFragment extends BaseFragment
{
    public static final String TAG = "SnsBlogFragment";

    private EditText idEdit;
    private Button nextBtn;
    private int type;
    

    public static SnsBlogFragment newInstance(int type)
    {
        final SnsBlogFragment f = new SnsBlogFragment();
		final Bundle args = new Bundle();
		args.putInt("blogType", type);
		f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getInt("blogType");
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_sns_get, container, false);

        idEdit = (EditText) v.findViewById(R.id.naverIdEdit);
        v.findViewById(R.id.naverApiEdit).setVisibility(View.GONE);
        nextBtn = (Button) v.findViewById(R.id.nextBtn);
        
        switch (type) {
			case Constants.REQUEST_SNS_BLOG_NAVER:
				idEdit.setText(DbController.queryBlogNaver(getSherlockActivity()));
				break;
			case Constants.REQUEST_SNS_BLOG_DAUM:
				idEdit.setText(DbController.queryBlogDaum(getSherlockActivity()));
				break;
			case Constants.REQUEST_SNS_BLOG_TSTORY:
				idEdit.setText(DbController.queryBlogTstory(getSherlockActivity()));
				break;
			case Constants.REQUEST_SNS_KAKAO_CH:
				idEdit.setText(DbController.querySnsKakaoCh(getSherlockActivity()));
				break;
		}

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        nextBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                String id = idEdit.getText().toString();
                onSnsBlogBtnClicked(id);
            }
        });
    }

    public void onSnsBlogBtnClicked(final String id)
    {
        if (CommonUtil.PatternUtil.isEmpty(id))
        {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_id);
            return;
        }
        
        switch (type) {
			case Constants.REQUEST_SNS_BLOG_NAVER:
				DbController.updateBlogNaver(getSherlockActivity(), id);
				break;
			case Constants.REQUEST_SNS_BLOG_DAUM:
				DbController.updateBlogDaum(getSherlockActivity(), id);
				break;
			case Constants.REQUEST_SNS_BLOG_TSTORY:
				DbController.updateBlogTstory(getSherlockActivity(), id);
				break;
			case Constants.REQUEST_SNS_KAKAO_CH:
				DbController.updateSnsKakaoCh(getSherlockActivity(), id);
				break;
		}     
        
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
