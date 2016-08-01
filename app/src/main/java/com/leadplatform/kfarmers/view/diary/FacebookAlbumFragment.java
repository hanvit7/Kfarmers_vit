package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.MyFaceBookAlbumJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;

import java.util.ArrayList;
import java.util.List;

public class FacebookAlbumFragment extends BaseRefreshMoreListFragment 
{
	public static final String TAG = "FacebookAlbumFragment";
	
	String nextData = "";
	
	CusListAdaper cusListAdaper;
	
	private boolean bMoreFlag = false;
	
	
    public static FacebookAlbumFragment newInstance()
    {
        final FacebookAlbumFragment f = new FacebookAlbumFragment();
        return f;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_facebook_album, container, false);
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		
		cusListAdaper.getItem(position);
		
		Intent data = new Intent();
		data.putExtra("id", cusListAdaper.getItem(position).id);
		getActivity().setResult(Activity.RESULT_OK, data);
		getActivity().finish();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (cusListAdaper == null) {
			cusListAdaper = new CusListAdaper(getSherlockActivity(), R.layout.item_text, new ArrayList<MyFaceBookAlbumJson>());
			setListAdapter(cusListAdaper);
			faceBookAlbumList("");
		}
	}
	
	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				faceBookAlbumList(nextData);
			} else {
				onLoadMoreComplete();
			}
		}
	};

	
	public void faceBookAlbumList(String next)
	{
		GraphRequest graphRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), "me/albums", new GraphRequest.Callback() {
			@Override
			public void onCompleted(GraphResponse graphResponse) {
				try {
					JsonNode data = JsonUtil.parseTree(graphResponse.getJSONObject().toString());
					JsonNode subData = data.get("data");

					for(JsonNode node : subData)
					{
						MyFaceBookAlbumJson bookJson = (MyFaceBookAlbumJson) JsonUtil.jsonToObject(node.toString(), MyFaceBookAlbumJson.class);

						cusListAdaper.add(bookJson);
					}

					if(data.get("paging").has("next"))
					{
						nextData = data.get("paging").get("cursors").get("after").textValue();
						bMoreFlag = true;
					}
					else
					{
						nextData = "";
						bMoreFlag = false;
					}


					cusListAdaper.notifyDataSetChanged();
					onRefreshComplete();
					onLoadMoreComplete();


				} catch (Exception e) {
					e.printStackTrace();
					onRefreshComplete();
					onLoadMoreComplete();
				}
			}
		});

		Bundle bundle = new Bundle();
		bundle.putString("fields","name");
		bundle.putString("after",next);

		graphRequest.setParameters(bundle);
		graphRequest.executeAsync();

		/*Request request = new Request(fSession, "me/albums",bundle,HttpMethod.GET,callback);
		RequestAsyncTask asyncTask = new RequestAsyncTask(request);
		asyncTask.execute();*/
	}
	
	public class CusListAdaper extends ArrayAdapter<MyFaceBookAlbumJson>
	{
		int resource;
		public CusListAdaper(Context context, int resource,List<MyFaceBookAlbumJson> objects) {
			super(context, resource, objects);
			
			this.resource = resource;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MyFaceBookAlbumJson albumJson = getItem(position);
			TextView textView ;
			
			if(convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resource, null);
			}

			textView = (TextView)convertView.findViewById(R.id.text);
			textView.setText(albumJson.name);
			return convertView;
		}
	}
}
	
