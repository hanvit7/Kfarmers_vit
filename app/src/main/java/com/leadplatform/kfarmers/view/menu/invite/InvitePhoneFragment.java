package com.leadplatform.kfarmers.view.menu.invite;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseListFragment;

import java.util.ArrayList;

public class InvitePhoneFragment extends BaseListFragment {

    ArrayList<String[]> searchList;
    ArrayList<String[]> arrayList;
    CustomListAdapter customListAdapter;
    EditText editText;

    public static InvitePhoneFragment newInstance() {
        final InvitePhoneFragment f = new InvitePhoneFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_invite_phone_list, container, false);

        editText = (EditText) v.findViewById(R.id.editText);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                findName(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (customListAdapter == null) {
            customListAdapter = new CustomListAdapter(getActivity(), R.layout.item_invite);
            setListAdapter(customListAdapter);

            new LoadContactsData().execute();
        }
    }

    private class LoadContactsData extends AsyncTask<Void, Void, Void> {
        private ProgressDialog Dialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            Dialog.setMessage("주소록 정보를 로딩 중입니다.");
            Dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ReadFromDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Dialog.dismiss();
            customListAdapter.addAll(arrayList);
            customListAdapter.notifyDataSetChanged();
        }
    }


    private void ReadFromDB() {
        arrayList = new ArrayList<>();

        String[] arrProjection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        String[] arrPhoneProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor clsCursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, arrProjection, ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        while (clsCursor.moveToNext()) {
            String strContactId = clsCursor.getString(0);

            if(strContactId != null) {
                Cursor clsPhoneCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrPhoneProjection
                        , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + strContactId, null, null);
                while (clsPhoneCursor.moveToNext()) {
                    String[] str = new String[2];
                    str[0] = clsCursor.getString(1);
                    str[1] = clsPhoneCursor.getString(0);

                    arrayList.add(str);
                }
                clsPhoneCursor.close();
            }
        }
        clsCursor.close();
    }

    final static char[] chosungWord = {
            0x3131, 0x3132, 0x3134, 0x3137, 0x3138,
            0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
            0x3146, 0x3147, 0x3148, 0x3149, 0x314a,
            0x314b, 0x314c, 0x314d, 0x314e
    };

    public String hangulToOnlyChosung(String s) {
        int chosungNum, tempNum;
        String resultString = "";

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != ' ') {
                if (ch >= 0xAC00 && ch <= 0xD7A3) {
                    tempNum = ch - 0xAC00;
                    chosungNum = tempNum / (21 * 28);

                    resultString += chosungWord[chosungNum];
                } else {
                    resultString += ch;
                }
            }
        }
        return resultString;
    }

    public void findName(String findString) {
        try {
            customListAdapter.clear();
            if (findString.length() > 0) {
                String onlyChosungString = hangulToOnlyChosung(findString);
                searchList = new ArrayList<>();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i) != null) {
                        String onlyChosungNameString = hangulToOnlyChosung(arrayList.get(i)[0]);
                        if (onlyChosungNameString.matches(onlyChosungString + ".*")) {
                            if (arrayList.get(i)[0] != null) searchList.add(arrayList.get(i));
                        }
                    }
                }
                customListAdapter.addAll(searchList);
                customListAdapter.notifyDataSetChanged();
            } else {
                customListAdapter.addAll(arrayList);
                customListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CustomListAdapter extends ArrayAdapter<String[]> {
        private Context context;
        private int itemLayoutResourceId;

        public CustomListAdapter(Context context, int resource) {
            super(context, resource);
            this.context = context;
            itemLayoutResourceId = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);
            }

            TextView name = ViewHolder.get(convertView, R.id.name);
            Button btn = ViewHolder.get(convertView, R.id.inviteBtn);

            name.setText(getItem(position)[0]);

            btn.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_INVITE, "Click_Invite", "연락처");
                    CommonUtil.AndroidUtil.actionSms(getActivity(),getItem(position)[1],getString(R.string.inviteSnsText));
                }
            });

            return convertView;
        }
    }
}

