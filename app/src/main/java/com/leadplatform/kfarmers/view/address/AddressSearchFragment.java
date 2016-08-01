package com.leadplatform.kfarmers.view.address;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CommonApiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.DataGoAddressJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.xml.JSONException;
import com.leadplatform.kfarmers.util.xml.JSONObject;
import com.leadplatform.kfarmers.util.xml.XML;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;
import com.leadplatform.kfarmers.view.payment.PaymentActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

public class AddressSearchFragment extends BaseDialogFragment {
	public static final String TAG = "AddressSearchFragment";

	private final String jibunApiKey = "mKZw7jCiANKZNgiWoLoj%2BXhyvNW34RB6zZ9Jk80zWDWz19yzZINGOfF9hovKTDPyQMEs9S3C%2FVdzal0pPvFheQ%3D%3D";
	private final String jibunApiUrl = "http://openapi.epost.go.kr/postal/retrieveLotNumberAdressService/retrieveLotNumberAdressService/getDetailList";
	private final String roadApiUrl = "http://openapi.epost.go.kr/postal/retrieveLotNumberAdressService/retrieveLotNumberAdressService/getLotNumberList";

	private Button address_search;
	private EditText address_input;
	private ListView listView;

	private AddressAdapter adapter;
	
	private RelativeLayout progressBar;
	
	private TextView nullText;

	public static AddressSearchFragment newInstance() {
		final AddressSearchFragment f = new AddressSearchFragment();

		final Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
		setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_address_search,
				container, false);

		address_search = (Button) v.findViewById(R.id.address_search);

		address_input = (EditText) v.findViewById(R.id.address_input);

		listView = (ListView) v.findViewById(R.id.listview);
		
		progressBar = (RelativeLayout) v.findViewById(R.id.progress);
		
		nullText = (TextView) v.findViewById(R.id.nullText);
		
		listView.setDivider(null);
		listView.setDividerHeight(0);

        address_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){ // 뷰의 id를 식별, 키보드의 완료 키 입력 검출
                    addressSerarch();
                }
                return false;
            }
        });

		address_search.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) {
                addressSerarch();
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) 
			{
                if(getActivity().getClass().getSimpleName().equals(AddressActivity.class.getSimpleName()))
                {
                    ((AddressActivity)getActivity()).setSearchData(adapter.getItem(arg2));
                    ((AddressActivity)getActivity()).hideSearchFragment();
                }
                else if(getActivity().getClass().getSimpleName().equals(PaymentActivity.class.getSimpleName()))
                {
                    ((PaymentActivity)getActivity()).setSearchData(adapter.getItem(arg2));
                    ((PaymentActivity)getActivity()).hideSearchFragment();
                }
			}
		});

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	public void addressSerarch() {

		if (address_input.getText().toString().trim().length() <= 0)
        {
            UiDialog.showDialog(getSherlockActivity(), R.string.dialog_empty_search);
            return;
        }

        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(address_input.getWindowToken(), 0);

		progressBar.setVisibility(View.VISIBLE);

		CommonApiController.getSearchJibunAddress(jibunApiUrl, jibunApiKey,
				address_input.getText().toString().trim(),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						List<DataGoAddressJson> arrayList = null;
						progressBar.setVisibility(View.GONE);
						JSONObject jsonObj = XML.toJSONObject(new String(arg2).toString());

						if (jsonObj.getJSONObject("DetailListResponse").getJSONObject("cmmMsgHeader").getString("returnCode").equals("00")) {
							try {
								JsonNode root = JsonUtil.parseTree(jsonObj.getJSONObject("DetailListResponse").opt("detailList").toString());

								if (root.isArray()) {
									arrayList = (List<DataGoAddressJson>) JsonUtil.jsonToArrayObject(root, DataGoAddressJson.class);
								} else {
									arrayList = new ArrayList();
									arrayList.add((DataGoAddressJson) JsonUtil.jsonToObject(root.toString(), DataGoAddressJson.class));
								}
								/*ObjectMapper mapper = new ObjectMapper();
								mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
								
								List<DataGoAddressJson> arrayList = mapper.convertValue(root, mapper.getTypeFactory().constructCollectionType(List.class, DataGoAddressJson.class));*/
								adapter = new AddressAdapter(getActivity(), R.layout.item_address, arrayList);
								listView.setAdapter(adapter);
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}


						if (null != arrayList && arrayList.size() > 0) {
							nullText.setVisibility(View.GONE);
						} else {
							nullText.setVisibility(View.VISIBLE);
							if (null != adapter) {
								adapter.clear();
							}
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
										  Throwable arg3) {
						progressBar.setVisibility(View.GONE);
					}
				});
	}

	public class AddressAdapter extends ArrayAdapter<DataGoAddressJson> {

		int resource;

		public AddressAdapter(Context context, int resource,
				List<DataGoAddressJson> objects) {
			super(context, resource, objects);
			this.resource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resource, null);
			}

			TextView zip = ViewHolder.get(convertView, R.id.address_zip);
			TextView text = ViewHolder.get(convertView, R.id.address_text);

			final DataGoAddressJson item = getItem(position);

			if (item != null) 
			{
				zip.setText(item.zipNo);
				text.setText(item.adres);
			}
			return convertView;
		}
	}
}
