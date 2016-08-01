package com.leadplatform.kfarmers.view.menu.release;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ReleaseListHolder;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;

public class ReleaseListAdapter extends ArrayAdapter<CategoryJson>
{
    private Context context;
    private int itemLayoutResourceId;
    private ViewOnClickListener listener;

    public ReleaseListAdapter(Context context, int itemLayoutResourceId, ArrayList<CategoryJson> items, ViewOnClickListener listener)
    {
        super(context, itemLayoutResourceId, items);
        this.context = context;
        this.itemLayoutResourceId = itemLayoutResourceId;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ReleaseListHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(itemLayoutResourceId, null);

            holder = new ReleaseListHolder();
            holder.Category = (TextView) convertView.findViewById(R.id.Category);
            holder.Edit = (Button) convertView.findViewById(R.id.Edit);
            holder.Edit.setTag(position);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ReleaseListHolder) convertView.getTag();
        }

        CategoryJson item = getItem(position);

        if (item != null)
        {
            if (!PatternUtil.isEmpty(item.SubName))
            {
                holder.Category.setText(item.SubName);
            }
            else
            {
                holder.Category.setVisibility(View.INVISIBLE);
            }

            holder.Edit.setOnClickListener(listener);
        }

        return convertView;
    }
}
