package com.leadplatform.kfarmers.view.address;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.AndroidUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class AddressAddFragment extends BaseDialogFragment {
	public static final String TAG = "AddressAddFragment";

	public Button address_searchBtn,okBtn,cancelBtn;
	public EditText addr1,addr2,name,phone;
	
	AddressJson addressAdd;
	
	boolean isMody = false;

	public static AddressAddFragment newInstance(boolean isMody,AddressJson addressJson) {
		final AddressAddFragment f = new AddressAddFragment();

		final Bundle args = new Bundle();
		args.putBoolean("isMody", isMody);
		
		if(null != addressJson)
		{
			args.putSerializable("addressJson", addressJson);
		}
		f.setArguments(args);
		
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			isMody = getArguments().getBoolean("isMody");
			addressAdd = (AddressJson) getArguments().getSerializable("addressJson");
		}

		setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_address, container,
				false);
		
		address_searchBtn = (Button) v.findViewById(R.id.address_search);
		okBtn = (Button) v.findViewById(R.id.okBtn);
		cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
		addr1 = (EditText) v.findViewById(R.id.addr1);
		addr2 = (EditText) v.findViewById(R.id.addr2);
		name = (EditText) v.findViewById(R.id.name);
		phone = (EditText) v.findViewById(R.id.phone);

		address_searchBtn.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) 
			{
				((AddressActivity)getActivity()).showSearchFragment();
			}
		});
		
		addr1.setOnClickListener(new ViewOnClickListener() {
			
			@Override
			public void viewOnClick(View v) {
				((AddressActivity)getActivity()).showSearchFragment();
			}
		});
		
		okBtn.setOnClickListener(new ViewOnClickListener() {
			
			@Override
			public void viewOnClick(View v) {
				
				if(PatternUtil.isEmpty(name.getText().toString().trim()))
				{
					UiDialog.showDialog(getSherlockActivity(), R.string.dialog_address_name);
					return;
				}
				if(!PatternUtil.isValidPhone(CommonUtil.PatternUtil.convertPhoneFormat(phone.getText().toString().trim())))
				{
					UiDialog.showDialog(getSherlockActivity(), R.string.dialog_address_phone);
					return;
				}
				if(PatternUtil.isEmpty(addr1.getText().toString().trim()))
				{
					UiDialog.showDialog(getSherlockActivity(), R.string.dialog_address_address);
					return;
				}
				if(PatternUtil.isEmpty(addr2.getText().toString().trim()))
				{
					UiDialog.showDialog(getSherlockActivity(), R.string.dialog_address_address);
					return;
				}
				
				addressAdd.setShippingName(name.getText().toString().trim());
				addressAdd.setAddress2(addr2.getText().toString().trim());
				addressAdd.setPhoneNo(CommonUtil.PatternUtil.convertPhoneFormat(phone.getText().toString().trim()));

				if(!isMody)
				{
					((AddressActivity)getActivity()).addressAdd(addressAdd);	
				}
				else
				{
					((AddressActivity)getActivity()).addressModify(addressAdd);
				}
				
			}
		});
		
		cancelBtn.setOnClickListener(new ViewOnClickListener() {
			
			@Override
			public void viewOnClick(View v) {
				((AddressActivity)getActivity()).hideAddFragment();
			}
		});
		
		if(addressAdd == null)
		{
			addressAdd = new AddressJson();
		}
		
		if(isMody)
		{
			name.setText(addressAdd.getShippingName());
			phone.setText(addressAdd.getPhoneNo().replace("-", ""));
			addr1.setText(addressAdd.getAddress());
			addr2.setText(addressAdd.getAddress2());
		}
		else
		{
			
			String phoneNm = AndroidUtil.getPhoneNumber(getSherlockActivity());
			if(phoneNm == null)
			{
				phoneNm="";
			}
			
			phone.setText(phoneNm);	
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}

