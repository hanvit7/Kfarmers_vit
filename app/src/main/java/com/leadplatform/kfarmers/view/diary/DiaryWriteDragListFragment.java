package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.holder.DiaryWriteHolder;
import com.leadplatform.kfarmers.model.item.WDiaryItem;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseDragListFragment;
import com.leadplatform.kfarmers.view.base.OnLodingCompleteListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

public class DiaryWriteDragListFragment extends BaseDragListFragment
{
    public static final String TAG = "DiaryWriteDragListFragment";

    public static final int MAX_PICTURE_COUNT = 5;
    public static final int MAX_FACEBOOK_PICTURE_COUNT = 10;

    private int diaryType;
    private ArrayList<WDiaryItem> itemArrayList;
    private DragAdapter dragAdapter;
    private DisplayImageOptions options;
    
	OnLodingCompleteListener mOnLodingCompleteListener;

    public static DiaryWriteDragListFragment newInstance(int type)
    {
        final DiaryWriteDragListFragment f = new DiaryWriteDragListFragment();

        final Bundle args = new Bundle();
        args.putInt("type", type);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            diaryType = getArguments().getInt("type");
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mOnLodingCompleteListener = (OnLodingCompleteListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnLodingcompleteListener");
		}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final DragSortListView dragListView = (DragSortListView) inflater.inflate(R.layout.fragment_write_diary_draglist, container, false);

        DragSortController dragListController = buildController(dragListView);
        dragListView.setFloatViewManager(dragListController);
        dragListView.setOnTouchListener(dragListController);
        dragListView.setDragEnabled(true);

        options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).cacheOnDisk(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();

        return dragListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        DragSortListView dragListView = (DragSortListView) getListView();
        dragListView.setDropListener(onDropListener);
        dragListView.setRemoveListener(onRemoveListener);

        WDiaryItem item = new WDiaryItem();
        item.setType(WDiaryItem.TEXT_TYPE);
        itemArrayList = new ArrayList<WDiaryItem>();
        itemArrayList.add(item);
        dragAdapter = new DragAdapter(getSherlockActivity(), R.layout.item_write_diary, itemArrayList, options);
        // getListView().setItemsCanFocus(true);
        // getListView().setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        // getListView().setOnScrollListener(new OnScrollListener()
        // {
        // @Override
        // public void onScrollStateChanged(AbsListView view, int scrollState)
        // {
        // if (scrollState == 1)
        // {
        // Log.e(TAG, "================ onScrollStateChanged ==");
        // view.requestFocus();
        // }
        // }
        //
        // @Override
        // public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3)
        // {
        //
        // }
        // });

        getListView().setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                WDiaryItem item = dragAdapter.getItem(position);
                if (item.getType() == WDiaryItem.TEXT_TYPE)
                {
                    Intent intent = new Intent(getSherlockActivity(), DiaryEditActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("textContent", item.getTextContent());
                    startActivityForResult(intent, Constants.REQUEST_EDIT_DIARY);
                }
            }
        });

        // getListView().setOnItemSelectedListener(new OnItemSelectedListener()
        // {
        // @Override
        // public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        // {
        // Log.e(TAG, "================ setOnItemSelectedListener ==");
        // }
        //
        // @Override
        // public void onNothingSelected(AdapterView<?> arg0)
        // {
        //
        // }
        // });

        setListAdapter(dragAdapter);
        
        if(diaryType == DiaryWriteActivity.TYPE_DIARY_BLOG_TO_LIST)
        {
        	mOnLodingCompleteListener.OnLodingComplete(TAG,true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == Constants.REQUEST_EDIT_DIARY)
            {
                int position = data.getIntExtra("position", 0);
                String textContent = data.getStringExtra("textContent");
                WDiaryItem item = dragAdapter.getItem(position);
                item.setTextContent(textContent);
                dragAdapter.remove(item);
                dragAdapter.insert(item, position);
                dragAdapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        if (getSherlockActivity() instanceof DiaryWriteActivity)
        {
            getSherlockActivity().getMenuInflater().inflate(R.menu.menu_w_diary, menu);
            menu.setHeaderTitle(R.string.context_menu_edit_title);
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.btn_input_text_up:
                if (getSherlockActivity() instanceof DiaryWriteActivity)
                {
                    if(diaryType == DiaryWriteActivity.TYPE_DIARY_NORMAL)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE, "Click_Write-Menu", "위에 글 추가");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_EDIT)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_MODIFY, "Click_Write-Menu", "위에 글 추가");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_BLOG_TO_LIST)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Write-Menu", "위에 글 추가");
                    }
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    addUpListViewTextItem(info.position);
                }
                return true;

            case R.id.btn_input_text_down:
                if (getSherlockActivity() instanceof DiaryWriteActivity)
                {
                    if(diaryType == DiaryWriteActivity.TYPE_DIARY_NORMAL)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE, "Click_Write-Menu", "아래 글 추가");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_EDIT)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_MODIFY, "Click_Write-Menu", "아래 글 추가");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_BLOG_TO_LIST)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Write-Menu", "아래 글 추가");
                    }
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    addDownListViewTextItem(info.position);
                }
                return true;

            case R.id.btn_delete:
                if (getSherlockActivity() instanceof DiaryWriteActivity)
                {
                    if(diaryType == DiaryWriteActivity.TYPE_DIARY_NORMAL)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE, "Click_Write-Menu", "삭제");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_EDIT)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_MODIFY, "Click_Write-Menu", "삭제");
                    }
                    else if(diaryType == DiaryWriteActivity.TYPE_DIARY_BLOG_TO_LIST)
                    {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_WRITE_SNS, "Click_Write-Menu", "삭제");
                    }
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    deleteListViewItem(info.position);
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private final DragSortListView.DropListener onDropListener = new DragSortListView.DropListener()
    {
        @Override
        public void drop(int from, int to)
        {
            if (dragAdapter == null)
                return;

            if (from != to)
            {
                WDiaryItem item = dragAdapter.getItem(from);
                dragAdapter.remove(item);
                dragAdapter.insert(item, to);
            }
        }
    };

    private DragSortListView.RemoveListener onRemoveListener = new DragSortListView.RemoveListener()
    {
        @Override
        public void remove(int which)
        {
            if (dragAdapter == null)
                return;

            dragAdapter.remove(dragAdapter.getItem(which));
        }
    };

    @Override
    public int getDragHandleId()
    {
        return R.id.dragImg;
    }

    @Override
    public int getClickRemoveId()
    {
        return 0;
    }

    public void addUpListViewTextItem(int position)
    {
        if (dragAdapter != null)
        {
            WDiaryItem addItem = new WDiaryItem();
            addItem.setType(WDiaryItem.TEXT_TYPE);

            dragAdapter.insert(addItem, position);
            dragAdapter.notifyDataSetChanged();
        }
    }

    public void addDownListViewTextItem(int position)
    {
        if (dragAdapter != null)
        {
            WDiaryItem addItem = new WDiaryItem();
            addItem.setType(WDiaryItem.TEXT_TYPE);

            position += 1;

            dragAdapter.insert(addItem, position);
            dragAdapter.notifyDataSetChanged();
            getListView().smoothScrollToPosition(dragAdapter.getCount());
        }
    }

    public void addListViewPictureItem(ArrayList<String> imgPath)
    {
        if (dragAdapter != null)
        {
        	for(String path : imgPath)
        	{
        		WDiaryItem item = new WDiaryItem();
                item.setType(WDiaryItem.PICTURE_TYPE);
                item.setPictureContent(path);
                dragAdapter.add(item);        		
        	}
            dragAdapter.notifyDataSetChanged();
            getListView().smoothScrollToPosition(dragAdapter.getCount());
        }
    }

    public void deleteListViewItem(int position)
    {
        if (dragAdapter != null)
        {
            dragAdapter.remove(dragAdapter.getItem(position));
            dragAdapter.notifyDataSetChanged();
        }
    }

    public int isPictureCount()
    {
        if (dragAdapter != null)
        {
            int pictureCount = 0;
            for (int index = 0; index < dragAdapter.getCount(); index++)
            {
                WDiaryItem item = dragAdapter.getItem(index);
                if (item.getType() == WDiaryItem.PICTURE_TYPE)
                    pictureCount++;
            }

            return pictureCount;
        }
        return 0;
    }
    
    public int nowPictureCount()
    {
        if (dragAdapter != null)
        {
            int pictureCount = 0;
            for (int index = 0; index < dragAdapter.getCount(); index++)
            {
                WDiaryItem item = dragAdapter.getItem(index);
                if (item.getType() == WDiaryItem.PICTURE_TYPE)
                    pictureCount++;
            }

            return pictureCount;
        }
        return 0;
    }

    public class DragAdapter extends ArrayAdapter<WDiaryItem>
    {
        private int itemLayoutResourceId;
        private DisplayImageOptions options;

        public DragAdapter(Context context, int itemLayoutResourceId, ArrayList<WDiaryItem> items, DisplayImageOptions options)
        {
            super(context, itemLayoutResourceId, items);
            this.itemLayoutResourceId = itemLayoutResourceId;
            this.options = options;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            DiaryWriteHolder holder;

            if (convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);

                holder = new DiaryWriteHolder();
                holder.textLayout = (RelativeLayout) convertView.findViewById(R.id.textLayout);
                holder.textEdit = (TextView) convertView.findViewById(R.id.textEdit);
                holder.pictureLayout = (RelativeLayout) convertView.findViewById(R.id.pictureLayout);
                holder.pictureImg = (ImageView) convertView.findViewById(R.id.pictureImg);
                holder.dragImg = (ImageView) convertView.findViewById(R.id.dragImg);

                convertView.setTag(holder);
            }
            else
            {
                holder = (DiaryWriteHolder) convertView.getTag();
            }

            WDiaryItem item = getItem(position);

            if (item != null)
            {
                if (item.getType() == WDiaryItem.TEXT_TYPE)
                {
                    holder.textLayout.setVisibility(View.VISIBLE);
                    holder.pictureLayout.setVisibility(View.GONE);
                    holder.textEdit.setText(item.getTextContent());
                    holder.textEdit.setTag(position);
                    // holder.textEdit.setOnFocusChangeListener(new OnFocusChangeListener()
                    // {
                    // @Override
                    // public void onFocusChange(final View v, boolean hasFocus)
                    // {
                    // Log.e(TAG, "================ onFocusChange == " + hasFocus);
                    // if (!hasFocus)
                    // {
                    // final int position = v.getId();
                    // final EditText textEdit = (EditText) v;
                    // getItem(position).setTextContent(textEdit.getText().toString());
                    // }
                    // else
                    // {
                    // // new Handler().postDelayed(new Runnable()
                    // // {
                    // // @Override
                    // // public void run()
                    // // {
                    // // EditText edit = (EditText) v;
                    // // if (!v.isFocused())
                    // // {
                    // // Log.e(TAG, "================ postDelayed == ");
                    // // edit.requestFocus();
                    // // Selection.setSelection(edit.getText(), edit.length());
                    // // }
                    // // }
                    // // }, 200);
                    // }
                    // }
                    // });

                    // holder.textEdit.setOnClickListener(new View.OnClickListener()
                    // {
                    // @Override
                    // public void onClick(View view)
                    // {
                    // int position = (Integer) view.getTag();
                    // WDiaryItem item = getItem(position);
                    // Intent intent = new Intent(getSherlockActivity(), DiaryEditActivity.class);
                    // intent.putExtra("position", position);
                    // intent.putExtra("textContent", item.getTextContent());
                    // startActivityForResult(intent, Constants.REQUEST_EDIT_DIARY);
                    // }
                    // });
                }
                else
                {
                    holder.textLayout.setVisibility(View.GONE);
                    holder.pictureLayout.setVisibility(View.VISIBLE);

                    if (getSherlockActivity() instanceof DiaryWriteActivity)
                    {
                        if (item.getPictureContent().contains("http"))
                        {
                            ((DiaryWriteActivity) getSherlockActivity()).imageLoader.displayImage(item.getPictureContent(), holder.pictureImg,
                                    options);
                        }
                        else
                        {
                            ((DiaryWriteActivity) getSherlockActivity()).imageLoader.displayImage("file://" + item.getPictureContent(),
                                    holder.pictureImg, options);
                        }
                    }
                }
            }

            return convertView;
        }
    }
}
