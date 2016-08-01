package com.leadplatform.kfarmers.view.event;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.APIErrorResult;
import com.kakao.MyStoryInfo;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.DialogOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.snipe.EventCommentJson;
import com.leadplatform.kfarmers.model.json.snipe.EventJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.kakao.KakaoStory;
import com.leadplatform.kfarmers.util.kakao.MyKakaoStoryHttpResponseHandler;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.farm.FarmSlideImageAdapter;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.leadplatform.kfarmers.view.sns.SnsActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class EventDetailFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "EventDetailFragment";

    private final int mLimit = 20;
    private  int mPage = 1;
    private boolean mMoreFlag = false;

    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOption;

    private String mEventIdx;

    private Button mEventBtn;

    private View mHeaderView;
    private ViewPager mEventViewpager;
    private LinearLayout mEventViewpagerPaging;

    private ListAdapter mListAdapter;

    private View mEventDiaglogView;

    private EventJson mEventJson;

    private String mKakaoContent,mUserName,mUserPhone,mUserComment;

    private ProgressDialog mDialog;

    private UserDb mUserData;

    private LayoutInflater mInflater;

    private AlertDialog mAlertDialog;

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (mMoreFlag == true) {
                mMoreFlag = false;
                getComentData(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getComentData(true);
        }
    };

    public static EventDetailFragment newInstance(String orderNo) {
        final EventDetailFragment f = new EventDetailFragment();
        final Bundle args = new Bundle();
        args.putString(EventDetailActivity.EVENT_IDX,orderNo);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEventIdx = getArguments().getString(EventDetailActivity.EVENT_IDX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_event_detail, container, false);
        mInflater = inflater;

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();

        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);

        mEventBtn = (Button) v.findViewById(R.id.EventBtn);

        mHeaderView = inflater.inflate(R.layout.item_event_header, null, false);
        mEventViewpager = (ViewPager) mHeaderView.findViewById(R.id.EventViewpager);
        mEventViewpagerPaging = (LinearLayout) mHeaderView.findViewById(R.id.EventViewpagerPaging);

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        getListView().addHeaderView(mHeaderView);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        if (mListAdapter  == null) {
            mListAdapter = new ListAdapter(getActivity(), R.layout.item_event_reply);
            getListView().setAdapter(mListAdapter);
            getEventData();
        }
    }

    private void kakaoEvent()
    {
        if (com.kakao.Session.initializeSession(getActivity(), new SessionCallback() {
            @Override
            public void onSessionOpened()
            {
                UiController.hideProgressDialog(getActivity());
                eventDiaglog();
            }
            @Override
            public void onSessionClosed(KakaoException exception)
            {
                UiController.hideProgressDialog(getActivity());
                DbController.updateKakaoFlag(getActivity(), false);
            }
        }))
        {
            UiController.showProgressDialog(getActivity());
        } else if (com.kakao.Session.getCurrentSession().isOpened()) {
            UiController.hideProgressDialog(getActivity());
            eventDiaglog();
        } else {
            Intent intent = new Intent(getActivity(), SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO);
            startActivityForResult(intent, Constants.REQUEST_SNS_KAKAO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_SNS_KAKAO) {
                DbController.updateKakaoFlag(getActivity(), true);
            }
        }
    }

    private void eventDiaglog()
    {
        ProfileJson profileJson = null;
        try
        {
            UserDb user = DbController.queryCurrentUser(getActivity());
            JsonNode root  = JsonUtil.parseTree(user.getProfileContent());
            profileJson= (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mEventDiaglogView = mInflater.inflate(R.layout.item_event_dialog, null, false);

        if(profileJson.Name != null && !profileJson.Name.isEmpty())
            ((EditText)mEventDiaglogView.findViewById(R.id.Name)).setText(profileJson.Name);
        if(profileJson.Phone2 != null && !profileJson.Phone2.isEmpty())
            ((EditText)mEventDiaglogView.findViewById(R.id.Phone)).setText(profileJson.Phone2.replaceAll("-",""));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("참여하기");
        dialogBuilder.setView(mEventDiaglogView);
        dialogBuilder.setPositiveButton(getString(R.string.dialog_ok), new DialogOnClickListener() {
            @Override
            public void dialogOnClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.dialog_cancel), new DialogOnClickListener() {
            @Override
            public void dialogOnClick(DialogInterface dialog, int which) {
            }
        });
        mAlertDialog = dialogBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //alertDialog.dismiss();
                        mUserComment = ((EditText) mEventDiaglogView.findViewById(R.id.Comment)).getText().toString().trim();
                        mUserName = ((EditText) mEventDiaglogView.findViewById(R.id.Name)).getText().toString().trim();
                        mUserPhone = ((EditText) mEventDiaglogView.findViewById(R.id.Phone)).getText().toString().trim();

                        if (mUserName == null || mUserName.trim().isEmpty()) {
                            UiController.showDialog(getActivity(), "이름을 입력해주세요.");
                            return;
                        } else if (mUserPhone == null || mUserPhone.trim().isEmpty()) {
                            UiController.showDialog(getActivity(), "연락처를 입력해주세요.");
                            return;
                        } else if (mUserComment == null || mUserComment.trim().isEmpty()) {
                            UiController.showDialog(getActivity(), "댓글을 입력해주세요.");
                            return;
                        } else if (mUserComment.length()<20)
                        {
                            UiController.showDialog(getActivity(), "댓글을 20자 이상 입력해주세요.");
                            return;
                        }

                        mKakaoContent = ((EditText) mEventDiaglogView.findViewById(R.id.Comment)).getText().toString().trim();

                        mKakaoContent += "\n\n";
                        mKakaoContent += mEventJson.content_title.trim();

                        mKakaoContent += "\n\n";
                        mKakaoContent += Html.fromHtml(mEventJson.content.trim()).toString();

                        while (mKakaoContent.endsWith("\n"))
                        {
                            mKakaoContent = mKakaoContent.substring(0,mKakaoContent.length()-1);
                        }

                        if (mEventJson.hashtag != null && !mEventJson.hashtag.isEmpty()) {
                            mKakaoContent += "\n\n";
                            mKakaoContent += mEventJson.hashtag;
                        }

                        if (mEventJson.link != null && !mEventJson.link.isEmpty()) {
                            mKakaoContent += "\n\n";
                            mKakaoContent += mEventJson.link;
                        }
                        mAlertDialog.dismiss();
                        kakaoSand();
                    }
                });
            }
        });
        mAlertDialog.show();

        /*UiDialog.showDialog(getActivity(), "이벤트 참여", mEventDiaglogView, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
            @Override
            public void onDialog(int type) {
                if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {

                    mUserComment = ((EditText) mEventDiaglogView.findViewById(R.id.Comment)).getText().toString().trim();
                    mUserName = ((EditText) mEventDiaglogView.findViewById(R.id.Name)).getText().toString().trim();
                    mUserPhone = ((EditText) mEventDiaglogView.findViewById(R.id.Phone)).getText().toString().trim();



                    if (mUserName == null || mUserName.trim().isEmpty()) {
                        UiController.showDialog(getActivity(), "이름을 입력해주세요.");
                        return;
                    } else if (mUserPhone == null || mUserPhone.trim().isEmpty()) {
                        UiController.showDialog(getActivity(), "연락처를 입력해주세요.");
                        return;
                    } else if (mUserComment == null || mUserComment.trim().isEmpty()) {
                        UiController.showDialog(getActivity(), "댓글을 입력해주세요.");
                        return;
                    } else if (mUserComment.length()<20)
                    {
                        UiController.showDialog(getActivity(), "댓글을 20자 이상 입력해주세요.");
                        return;
                    }

                    mKakaoContent = ((EditText) mEventDiaglogView.findViewById(R.id.Comment)).getText().toString().trim();

                    mKakaoContent += "\n\n";
                    mKakaoContent += mEventJson.content_title.trim();

                    mKakaoContent += "\n\n";
                    mKakaoContent += Html.fromHtml(mEventJson.content.trim()).toString();

                    if (mEventJson.link != null && !mEventJson.link.isEmpty()) {
                        mKakaoContent += "\n";
                        mKakaoContent += mEventJson.link;
                    }
                    kakaoSand();
                }
            }
        });*/
    }


    private void displayViewPagerIndicator(int position) {
        mEventViewpagerPaging.removeAllViews();

        for (int i = 0; i < mEventViewpager.getAdapter().getCount(); i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ImageView dot = new ImageView(getActivity());

            if (i != 0) {
                lp.setMargins(CommonUtil.AndroidUtil.pixelFromDp(getActivity(), 3), 0, 0, 0);
                dot.setLayoutParams(lp);
            }

            if (i == position) {
                dot.setImageResource(R.drawable.button_farm_paging_on);
            } else {
                dot.setImageResource(R.drawable.button_farm_paging_off);
            }
            mEventViewpagerPaging.addView(dot);
        }
    }

    private void displayFarmHeaderView() {

        ((TextView) getView().findViewById(R.id.Name)).setText(mEventJson.title);
        ((TextView) getView().findViewById(R.id.Title)).setText(mEventJson.sub_title);
        ((TextView) getView().findViewById(R.id.Date)).setText(mEventJson.start_date + " ~ " + mEventJson.end_date);
        ((TextView) getView().findViewById(R.id.ContentTitle)).setText(mEventJson.content_title);
        ((TextView) getView().findViewById(R.id.ContentDes)).setText(Html.fromHtml(mEventJson.content.trim()));
        ((TextView) getView().findViewById(R.id.EventInfo)).setText(Html.fromHtml(mEventJson.info));
        getView().findViewById(R.id.EventInfo).setVisibility(View.VISIBLE);

        Button mFarmBtn = (Button) getView().findViewById(R.id.ButtonFarm);
        Button mProductBtn = (Button) getView().findViewById(R.id.ButtonProduct);

        if(mEventJson.point != null && Integer.parseInt(mEventJson.point) > 0) {
            ((TextView) getView().findViewById(R.id.Point)).setText(mEventJson.point + "P 적립");
            ((TextView) getView().findViewById(R.id.Point)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) getView().findViewById(R.id.Point)).setVisibility(View.GONE);
        }

        if(mEventJson.status.equals("진행"))
        {
            ((TextView) getView().findViewById(R.id.Dday)).setText(mEventJson.duration);
        }
        else
        {
            ((TextView) getView().findViewById(R.id.Dday)).setText(mEventJson.status);
        }

        if(mEventJson.farm != null && !mEventJson.farm.isEmpty())
        {
            mFarmBtn.setVisibility(View.VISIBLE);
            mFarmBtn.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    Intent intent = new Intent(getActivity(), FarmActivity.class);
                    intent.putExtra("userType", "F");
                    intent.putExtra("userIndex", mEventJson.farm);
                    startActivity(intent);
                }
            });
        }

        if(mEventJson.product != null && !mEventJson.product.isEmpty())
        {
            mProductBtn.setVisibility(View.VISIBLE);
            mProductBtn.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    Intent intent = new Intent(getActivity(), ProductActivity.class);
                    intent.putExtra("productIndex", mEventJson.product);
                    startActivity(intent);
                }
            });
        }

        if(mEventJson.status.equals("진행") || mEventJson.status.equals("발표"))
        {
            mEventBtn.setBackgroundResource(R.drawable.btn_minicart_buy);
            if(mEventJson.status.equals("진행")) {
                mEventBtn.setText("참여하기");
            }
            else
            {
                mEventBtn.setText("당첨자 보기");
            }
        }
        else
        {
            mEventBtn.setBackgroundResource(R.drawable.btn_minicart_cart);

            if(mEventJson.status.equals("예정")) {
                mEventBtn.setText("예정");
            }
            else if(mEventJson.status.equals("종료")) {
                mEventBtn.setText("서포터즈 종료");
            }
        }

        mEventBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                if(mEventJson.status.equals("진행"))
                {
                    mUserData = DbController.queryCurrentUser(getActivity());

                    if (mUserData == null)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_DETAIL, "Click_Event", "비 로그인");

                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        try {
                            JsonNode root = JsonUtil.parseTree(mUserData.getProfileContent());
                            String type = root.findValue("Type").textValue();

                            if(type.equals("U")) {
                                /*String nowDate = CommonUtil.TimeUtil.simpleDateFormat(new Date(System.currentTimeMillis()));
                                ArrayList<String> data = AppPreferences.getEventData(getActivity());

                                if( null != data )
                                {
                                    if(!data.contains(nowDate))
                                    {
                                        kakaoEvent();
                                    }
                                    else
                                    {
                                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_DETAIL, "Click_Event", "일일참여 제한");
                                        UiController.showDialog(getActivity(),"서포터즈 참여는 하루 1회만 가능합니다.");
                                    }
                                }
                                else
                                {
                                    kakaoEvent();
                                }*/
                                kakaoEvent();
                            }else
                            {
                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_DETAIL, "Click_Event", "비 소비자");
                                UiController.showDialog(getActivity(),"소비자만 서포터즈 참여가 가능합니다.");
                            }
                        }catch (Exception e){}
                    }
                }
                else if(mEventJson.status.equals("발표"))
                {
                    View view = mInflater.inflate(R.layout.item_event_win, null, false);

                    if(mEventJson.prizewinner != null && !mEventJson.prizewinner.isEmpty())
                    {
                        ((TextView)view.findViewById(R.id.Content)).setText(Html.fromHtml(mEventJson.prizewinner));
                    }
                    if(mEventJson.file.prizewinner.path != null && !mEventJson.file.prizewinner.path.isEmpty())
                    {
                        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mEventJson.file.prizewinner.path, (ImageView) view.findViewById(R.id.Image), mImageOption, new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                view.findViewById(R.id.Image).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {

                            }
                        });
                    }
                    UiDialog.showDialog(getActivity(), "당첨자 리스트", view, R.string.dialog_ok, null);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_DETAIL, "Click_Event", "서포터즈 당첨자 보기");
                }
            }
        });

        mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+mEventJson.file.title.path, (ImageView) getView().findViewById(R.id.Image),mImageOption);

        ArrayList<String> images = new ArrayList<String>();

        for(EventJson.ImagePath.Path imagePath : mEventJson.file.image)
        {
            images.add(Constants.KFARMERS_SNIPE_IMG+imagePath.path);
        }

        if (images.size() != 0) {
            mEventViewpager.setAdapter(new FarmSlideImageAdapter(getActivity(), images, mImageLoader));
            displayViewPagerIndicator(0);
            mEventViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int arg0) {
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                @Override
                public void onPageScrollStateChanged(int position) {
                    displayViewPagerIndicator(mEventViewpager.getCurrentItem());
                }
            });
        } else {
            mEventViewpager.setVisibility(View.GONE);
        }


        getView().findViewById(R.id.HeaderLine).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.Dday).setVisibility(View.VISIBLE);
        mEventBtn.setVisibility(View.VISIBLE);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_EVENT_DETAIL,mEventJson.sub_title);
    }

    private void getEventData() {

        UiController.showProgressDialog(getActivity());

        SnipeApiController.getEventDetail(mEventIdx, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                UiController.hideProgressDialog(getActivity());
                try {
                    if (Code == 200) {
                        JsonNode root = JsonUtil.parseTree(content);

                        mEventJson = (EventJson) JsonUtil.jsonToObject(root.toString(), EventJson.class);
                        displayFarmHeaderView();
                        getComentData(true);
                    } else {
                        UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                UiController.hideProgressDialog(getActivity());

            }
        });
    }

    private void getComentData(boolean isClear) {

        if (isClear) {
            mPage = 1;
            mMoreFlag = false;
            mListAdapter.clear();
        }

        SnipeApiController.getEventComment(mEventIdx, String.valueOf(mPage), String.valueOf(mLimit), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if (Code == 200) {
                        JsonNode root = JsonUtil.parseTree(content).get("item");

                        if (root.size() > 0) {
                            List<EventCommentJson> arrayList = (List<EventCommentJson>) JsonUtil.jsonToArrayObject(root, EventCommentJson.class);
                            mListAdapter.addAll(arrayList);
                        }

                        if (root.size() == mLimit)
                            mMoreFlag = true;
                        else
                            mMoreFlag = false;
                        mPage++;

                    } else {
                        UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                onRefreshComplete();
                onLoadMoreComplete();
                //UiController.hideProgressDialog(EventDetailActivity.this);
            }
        });
    }

    public class ListAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;

        public ListAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(itemLayoutResourceId, null);
            }

            EventCommentJson item = (EventCommentJson) getItem(position);

            //ImageView img = ViewHolder.get(convertView, R.id.Image);
            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView des = ViewHolder.get(convertView, R.id.Description);
            TextView number = ViewHolder.get(convertView, R.id.Number);
            TextView date = ViewHolder.get(convertView, R.id.Date);

            name.setText(item.nickname);
            des.setText(item.comment);
            number.setText(item.number);
            date.setText(item.datetime);

            //imageLoader.displayImage("http://m.kfarmers.kr/CustomerImage/20150316190058_5506a9da60423_0.jpg",img);

            return convertView;
        }
    }

    void kakaoSand() {
        mDialog.setMessage("서포터즈 응원 중입니다.");
        mDialog.show();

        KfarmersAnalytics.onClick(KfarmersAnalytics.S_EVENT_DETAIL, "Click_Event", "서포터즈 참여");

        final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).build();
        AsyncTask<Void, Void, ArrayList<String> > task = new AsyncTask<Void, Void, ArrayList<String> >() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                ArrayList<String> oriImage = new ArrayList<>();

                for(EventJson.ImagePath.Path imagePath : mEventJson.file.image)
                {
                    oriImage.add(Constants.KFARMERS_SNIPE_IMG+imagePath.path);
                }

                ArrayList<String> img = new ArrayList<>();
                for (String path : oriImage) {

                    try {
                        if (path.contains("http")) {
                            if (mImageLoader.getDiskCache().get(path) == null || !mImageLoader.getDiskCache().get(path).isFile()) {
                                mImageLoader.loadImageSync(path, options);
                            }
                        }
                        Bitmap rotateBitmap = ImageUtil.makeBitmap(mImageLoader.getInstance().getDiskCache().get(path).getAbsolutePath(), Constants.RESIZE_IMAGE_WIDTH * Constants.RESIZE_IMAGE_HEIGHT);
                        File imgFile = ImageUtil.createTempImageFile(mImageLoader.getDiskCache().getDirectory());
                        ImageUtil.saveBitmapFile(getActivity(), imgFile, rotateBitmap);
                        img.add(imgFile.getAbsolutePath());

                        rotateBitmap.recycle();
                        rotateBitmap = null;
                    }catch (Exception e){}
                }
                return img;
            }

            @Override
            protected void onPostExecute(final ArrayList<String> strings) {
                super.onPostExecute(strings);

                SnipeApiController.insertEventJoin(mEventJson.idx, mUserData.getUserID(), mUserPhone, mUserName, mUserComment, new SnipeResponseListener(getActivity()) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        try {
                            if (Code == 200) {
                                ArrayList<File> imageArrayList = new ArrayList<>();
                                for(String path : strings)
                                {
                                    imageArrayList.add(new File(path));
                                }
                                KakaoStory.requestStoryPost(getActivity(), mKakaoContent, imageArrayList, new MyKakaoStoryHttpResponseHandler<MyStoryInfo>(getActivity()) {
                                    @Override
                                    protected void onHttpSuccess(MyStoryInfo resultObj) {
                                        if (resultObj != null) {
                                            checkEventidentify("T");
                                        } else {
                                            checkEventidentify("T");
                                        }
                                    }
                                    @Override
                                    protected void onFailure(APIErrorResult errorResult) {
                                        super.onFailure(errorResult);
                                        checkEventidentify("T");
                                    }
                                });
                            } else if (Code == 204) {
                                mDialog.dismiss();
                                UiController.showDialog(getActivity(), "이미 참여한 서포터즈 입니다.");
                            } else if (Code == 203) {
                                mDialog.dismiss();
                                UiController.showDialog(getActivity(), "종료된 서포터즈 입니다.");
                            } else {
                                mDialog.dismiss();
                                UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                            }
                        } catch (Exception e) {
                            mDialog.dismiss();
                            UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                        super.onFailure(statusCode, headers, content, error);
                        mDialog.dismiss();
                    }
                });
            }
        };
        task.execute();
    }

    public void checkEventidentify(final String identify)
    {
        SnipeApiController.getEventidentify(mEventJson.idx, mUserData.getUserID(), mUserPhone, identify, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                mDialog.dismiss();
                try {
                    if (Code == 200) {
                        UiController.showDialog(getActivity(), "서포터즈 참여가 완료 되었습니다.");
                        getComentData(true);

                        /*if(identify.equals("T"))
                        {
                            String nowDate = CommonUtil.TimeUtil.simpleDateFormat(new Date(System.currentTimeMillis()));
                            AppPreferences.setEventData(getActivity(),nowDate);
                        }*/

                    } else if (Code == 204) {
                        UiController.showDialog(getActivity(), "이미 참여한 서포터즈 입니다.");
                    }
                } catch (Exception e) {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                mDialog.dismiss();
                UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
            }
        });
    }
}

