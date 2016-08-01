package com.leadplatform.kfarmers.view.reply;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.RecipeReplyJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class NewReplyFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "NewReplyFragment";

    ImageLoader imageLoader;
    DisplayImageOptions mOptionsProfile;

    private final int limit = 20;
    private int page = 1;
    private boolean bMoreFlag = false;

    String idx = "";

    ItemAdapter mItemAdapter;

    private EditText mEditComment;
    private ImageView mImageSend;

    private UserDb mUserData;

    public static NewReplyFragment newInstance(String idx) {
        final NewReplyFragment f = new NewReplyFragment();
        final Bundle args = new Bundle();
        args.putString("idx", idx);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idx = getArguments().getString("idx");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        imageLoader = ((BaseFragmentActivity) getActivity()).imageLoader;
        mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 30) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();

        View v = inflater.inflate(R.layout.fragment_new_reply, container, false);


        mEditComment = (EditText) v.findViewById(R.id.commentEdit);
        mImageSend = (ImageView) v.findViewById(R.id.sendBtn);

        mImageSend.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                addReplyData();
            }
        });

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer_transparent, loadMoreListener);
        if (mItemAdapter == null) {
            mItemAdapter = new ItemAdapter(getActivity(), R.layout.item_reply);
            setListAdapter(mItemAdapter);
            getReplyData(true);
        }
    }

    private void addReplyData() {

        InputMethodManager mInputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(mEditComment.getWindowToken(), 0);

        if (AppPreferences.getLogin(getActivity())) {

            if(mEditComment.getText().toString().trim().isEmpty()) {
                UiController.showDialog(getActivity(),"댓글을 입력해주세요.");
                return;
            }

            mUserData = DbController.queryCurrentUser(getActivity());
            String comment = mEditComment.getText().toString().trim().replaceAll("\\n{2,}", "\n\n");

            SnipeApiController.addRecipeReply(idx, mUserData.getUserID(),comment, new SnipeResponseListener(getSherlockActivity()) {
                @Override
                public void onSuccess(int Code, String content, String error) {
                    Toast.makeText(getActivity(), "댓글을 등록했습니다.", Toast.LENGTH_SHORT).show();
                    mEditComment.setText("");
                    getReplyData(true);
                }
            });
        }
        else
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private void getReplyData(Boolean isClear) {
        if (isClear) {
            mItemAdapter.clear();
            bMoreFlag = false;
            page = 1;
        }
        SnipeApiController.setRecipeReply(idx, String.valueOf(page),String.valueOf(limit), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            List<RecipeReplyJson> arrayList = new ArrayList<RecipeReplyJson>();

                            if (root.path("item").size() > 0) {
                                arrayList = (List<RecipeReplyJson>) JsonUtil.jsonToArrayObject(root.path("item"), RecipeReplyJson.class);
                                mItemAdapter.addAll(arrayList);
                            }

                            if (arrayList.size() == limit)
                                bMoreFlag = true;
                            else
                                bMoreFlag = false;

                            page++;

                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getReplyData(true);
        }
    };

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (bMoreFlag == true) {
                bMoreFlag = false;
                getReplyData(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    public class ItemAdapter extends ArrayAdapter<RecipeReplyJson> {
        int resource;

        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d a h시 mm분 ";

        DisplayImageOptions optionsProfile;

        SimpleDateFormat format,format2;

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;

            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);

            optionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                    .showImageOnLoading(R.drawable.icon_empty_profile).build();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }
            RecipeReplyJson item = getItem(position);

            if( item != null) {
                ImageView profile = ViewHolder.get(convertView, R.id.profile);
                TextView name = ViewHolder.get(convertView, R.id.Name);
                TextView des = ViewHolder.get(convertView, R.id.Description);
                TextView date = ViewHolder.get(convertView, R.id.Date);

                name.setText(item.member_name);
                des.setText(item.comment);
                try {
                    date.setText(format2.format((format.parse(item.datetime).getTime())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(item.member_profile_image != null && !item.member_profile_image.trim().isEmpty()) {
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.member_profile_image, profile, optionsProfile);
                } else {
                    profile.setImageResource(R.drawable.icon_empty_profile);
                }
            }
            return convertView;
        }
    }
}
