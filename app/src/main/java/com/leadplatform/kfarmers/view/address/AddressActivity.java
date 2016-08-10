package com.leadplatform.kfarmers.view.address;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.model.json.DataGoAddressJson;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import java.util.List;

public class AddressActivity extends BaseFragmentActivity {
	public static final String TAG = "AddressActivity";

	Button addressAdd;

	ListView listView;
	AddressAdapter adapter;

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_address);

		addressAdd = (Button) findViewById(R.id.address_add);

		listView = (ListView) findViewById(R.id.listview);
		
		registerForContextMenu(listView);

		/*listView.setDivider(null);
		listView.setDividerHeight(0);*/

		addressAdd.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) 
			{
				if(adapter.getCount()<5)
				{
					showAddFragment(false,null);
				}
				else
				{
					UiDialog.showDialog(mContext, R.string.dialog_address_max);
				}
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				openContextMenu(arg1);
			}
		});
		
		addressGet();
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
		title.setText(R.string.address_title);

		initActionBarBack();
	}

	/***************************************************************/
	// Method
	/***************************************************************/
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
		getMenuInflater().inflate(R.menu.menu_address, menu);
		menu.setHeaderTitle(R.string.context_menu_edit_title);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	
        switch (item.getItemId())
        {
            case R.id.btn_address_modify:
            	showAddFragment(true, adapter.getItem(info.position));
                return true;

            case R.id.btn_address_del:
            	addressDel(adapter.getItem(info.position));
                return true;
        }
        return super.onContextItemSelected(item);
    }

	public class AddressAdapter extends ArrayAdapter<AddressJson> {

		int resource;

		public AddressAdapter(Context context, int resource,
				List<AddressJson> objects) {
			super(context, resource, objects);
			this.resource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resource, null);
			}

			TextView name = ViewHolder.get(convertView, R.id.name);
			TextView phone = ViewHolder.get(convertView, R.id.phone);
			TextView addr = ViewHolder.get(convertView, R.id.address);

			final AddressJson item = getItem(position);

			if (item != null) {
				name.setText(item.getShippingName());
				phone.setText(item.getPhoneNo());
				addr.setText("["+item.getZipCode()+"] "+ item.getAddress()+ " "+ item.getAddress2());
			}
			
			return convertView;
		}
	}
	
	public void addressGet()
	{
		/*TokenApiController.getAddress(mContext, new TokenResponseListener(mContext){
			@Override
			public void onSuccess(int Code, String content) {
				super.onSuccess(Code, content);
				
				try {
					switch (Code) {
					case 0000:
						JsonNode root = JsonUtil.parseTree(content).get("rstContent");
						List<AddressJson> arrayList = (List<AddressJson>) JsonUtil.jsonToArrayObject(root, AddressJson.class);
						adapter = new AddressAdapter(mContext, R.layout.item_address_list,arrayList);
						listView.setAdapter(adapter);
						break;
					}
				} catch (Exception e) {
					UiController.showDialog(mContext, R.string.dialog_unknown_error);
				}
				
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
			}
		});*/
	}

	public void addressAdd(AddressJson addressJson) {
		/*TokenApiController.addAddress(mContext, addressJson,
				new TokenResponseListener(mContext) {
					public void onSuccess(int Code, String content) {
						hideAddFragment();
						addressGet();
					}

                    public void onFailure(int statusCode,
							org.apache.http.Header[] headers, byte[] content,
							Throwable error) {
					}
                });*/
	}
	
	public void addressDel(AddressJson addressJson)
	{
		/*TokenApiController.delAddress(addressJson,
				new TokenResponseListener(mContext) {
					public void onSuccess(int Code, String content) {
						addressGet();
					}

                    public void onFailure(int statusCode,
							org.apache.http.Header[] headers, byte[] content,
							Throwable error) {
					}
                });*/
	}
	
	public void addressModify(AddressJson addressJson)
	{
		/*TokenApiController.modyAddress(addressJson,
				new TokenResponseListener(mContext) {
					public void onSuccess(int Code, String content) {
						hideAddFragment();
						addressGet();
					}

                    public void onFailure(int statusCode,
							org.apache.http.Header[] headers, byte[] content,
							Throwable error) {
					}
                });*/
	}


	public void onActionBarLeftBtnClicked() {
		setResult(RESULT_CANCELED);
		finish();
	}

	public void showAddFragment(boolean isMody , AddressJson addressJson) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		AddressAddFragment addressSubFragment = AddressAddFragment
				.newInstance(isMody,addressJson);
		ft.addToBackStack(null);

		addressSubFragment.show(ft, AddressAddFragment.TAG);
	}

	public void showSearchFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		AddressSearchFragment addressSearchFragment = AddressSearchFragment
				.newInstance();
		ft.addToBackStack(null);

		addressSearchFragment.show(ft, AddressSearchFragment.TAG);
	}

	public AddressAddFragment getAddFragment() {
		FragmentManager fm = getSupportFragmentManager();
		AddressAddFragment fragment = (AddressAddFragment) fm
				.findFragmentByTag(AddressAddFragment.TAG);
		if (fragment != null) {
			return fragment;
		}

		return null;
	}

	public AddressSearchFragment getSearchFragment() {
		FragmentManager fm = getSupportFragmentManager();
		AddressSearchFragment fragment = (AddressSearchFragment) fm
				.findFragmentByTag(AddressSearchFragment.TAG);
		if (fragment != null) {
			return fragment;
		}

		return null;
	}

	public void setSearchData(DataGoAddressJson json) {
		final AddressAddFragment fragment = getAddFragment();
		if (fragment != null) {
			fragment.addressAdd.setAddress(json.getAdres());
			fragment.addressAdd.setZipCode(json.getZipNo());
			fragment.addressAdd.setZipCodeCategory("1");
			fragment.addr1.setText("[" + json.getZipNo() + "] "
					+ json.getAdres());
			fragment.addr2.setText("");
			
			
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() 
				{
					fragment.addr2.requestFocus();
					InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					mgr.showSoftInput(fragment.addr2, 0);
				}
			}, 100);
		}
	}

	public void hideAddFragment() {
		AddressAddFragment fragment = getAddFragment();
		if (fragment != null) {
			fragment.dismiss();
		}
	}

	public void hideSearchFragment() {
		AddressSearchFragment fragment = getSearchFragment();
		if (fragment != null) {
			fragment.dismiss();
		}
	}
}
