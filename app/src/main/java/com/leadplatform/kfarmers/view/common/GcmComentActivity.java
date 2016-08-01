package com.leadplatform.kfarmers.view.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.gcm.GcmIntentService;
import com.leadplatform.kfarmers.util.gcm.StartActivityForPush;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class GcmComentActivity extends BaseFragmentActivity {
	
	private HashMap<String, String> hashMap;
	private String title="";
	private String des="";
	private String boardType="";
	private String diaryIndex="";
	private String commentIndex="";
	private EditText editText;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(new View(mContext));
		Intent intent = null;
        if ((getIntent() == null) && (getIntent().getExtras() == null)) {
        	finish();
            return;
        }
        
        
        
        hashMap = (HashMap<String, String>) getIntent().getSerializableExtra(GcmIntentService.PUSH_BUNDLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        
        title = hashMap.get(GcmIntentService.PUSH_TITLE);
        des = hashMap.get(GcmIntentService.PUSH_MSG);
        boardType = hashMap.get(GcmIntentService.PUSH_COMMONT_BOARD_TYPE);
        diaryIndex = hashMap.get(GcmIntentService.PUSH_INDEX);
        commentIndex = hashMap.get(GcmIntentService.PUSH_SUBINDEX);
        
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_popup_comment, null);

		TextView textView = (TextView)view.findViewById(R.id.content);
		textView.setText(des);
		
		editText = (EditText)view.findViewById(R.id.commentEdit);

        Button button = (Button)view.findViewById(R.id.commentBtn);
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				String des = "";
				//String des = editText.getText().toString().trim();
				if(TextUtils.isEmpty(editText.getText().toString().trim()))
				{
					UiDialog.showDialog(mContext, "댓글을 입력해주세요.");
					return;
				}
				
				try 
				{
					des = title.split(">")[1]+"==]Name]=="+editText.getText().toString().trim();	
				} catch (Exception e) {
					des = editText.getText().toString().trim();
				}

				if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENTNOTICE"))
				{
					CenterController.writeCommentReview(new String[]{boardType}, new String[]{diaryIndex}, new String[]{commentIndex}, new String[]{des}, new CenterResponseListener(mContext) {
						@Override
						public void onSuccess(int Code, String content) {
							super.onSuccess(Code, content);
							try {
								switch (Code) {
									case 0000:

										Toast.makeText(mContext, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
										GcmComentActivity.this.finish();
										break;
									default:
										UiController.showDialog(mContext, R.string.dialog_unknown_error);
										GcmComentActivity.this.finish();
										break;
								}
							} catch (Exception e) {
								UiController.showDialog(mContext, R.string.dialog_unknown_error);
								GcmComentActivity.this.finish();
							}
						}
					});
				}
				else {
					CenterController.writeComment(new String[]{boardType},  new String[]{diaryIndex}, new String[]{commentIndex}, new String[]{des}, new CenterResponseListener(mContext) {
						@Override
						public void onSuccess(int Code, String content) {
							super.onSuccess(Code, content);
							try {
								switch (Code) {
									case 0000:

										Toast.makeText(mContext, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
										GcmComentActivity.this.finish();
										break;
									default:
										UiController.showDialog(mContext, R.string.dialog_unknown_error);
										GcmComentActivity.this.finish();
										break;
								}
							} catch (Exception e) {
								UiController.showDialog(mContext, R.string.dialog_unknown_error);
								GcmComentActivity.this.finish();
							}
						}
					});
				}


				/*TokenApiController.writeComment(mContext,new String[]{boardType}, new String[]{diaryIndex}, new String[]{commentIndex}, new String[]{des}, new TokenResponseListener(mContext){
					@Override
					public void onSuccess(int Code, String content) {
						super.onSuccess(Code, content);
						try {
							switch (Code) {
							case 0000:
								
								Toast.makeText(mContext,"댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
								GcmComentActivity.this.finish();
								
								*//*UiDialog.showDialog(mContext,"댓글이 등록되었습니다.",new CustomDialogListener() {
									@Override
									public void onDialog(int type) {
										GcmComentActivity.this.finish();		
									}
								});*//*
								break;
							default:
								UiController.showDialog(mContext, R.string.dialog_unknown_error);
								GcmComentActivity.this.finish();
								break;
							}
							
						} catch (Exception e) {
							UiController.showDialog(mContext, R.string.dialog_unknown_error);
							GcmComentActivity.this.finish();
						}
					}
					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] content, Throwable error) {
						super.onFailure(statusCode, headers, content, error);
						UiController.showDialog(mContext, R.string.dialog_unknown_error);
						GcmComentActivity.this.finish();
					}
				});*/
			}
		});
/*		UiDialog.showDialog(mContext,title,view, R.string.dialog_see, R.string.dialog_close, new CustomDialogListener() {
			
			@Override
			public void onDialog(int type) 
			{
				if(type == UiDialog.DIALOG_POSITIVE_LISTENER)
				{
					Intent intent = new Intent(getApplicationContext(), StartActivityForPush.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
					intent.putExtra(GcmIntentService.PUSH_BUNDLE,hashMap);
					
					mContext.startActivity(intent);
				}
				
				
				AppPreferences.setPushAllData(mContext, new ArrayList<String>());
				GcmComentActivity.this.finish();
				Log.d("ttest","닫기");
			}
		});*/
		
		AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));

		if (title != null) {
			alert.setTitle(title);
		}

		if (view != null) {
			alert.setView(view);
		}
		
		android.content.DialogInterface.OnClickListener mDefaultNegativeListener = new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				AppPreferences.setPushAllData(mContext, new ArrayList<String>());
				GcmComentActivity.this.finish();
			}
		};
		
		android.content.DialogInterface.OnClickListener mDefaultPositiveListener = new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(getApplicationContext(), StartActivityForPush.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(GcmIntentService.PUSH_BUNDLE,hashMap);
				mContext.startActivity(intent);
				
				AppPreferences.setPushAllData(mContext, new ArrayList<String>());
				GcmComentActivity.this.finish();
			}
		};
		
		alert.setPositiveButton(R.string.dialog_see, mDefaultPositiveListener);
		alert.setNegativeButton(R.string.dialog_close, mDefaultNegativeListener);
		alert.setCancelable(false);
		alert.show();
		
		Calendar calendar = Calendar.getInstance(); 
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		if(hour <=6 || hour >= 23)
		{
			
		}
		else
		{
			CommonUtil.setVibratorAndRing(mContext);	
		}
	}
	
	@Override
	public void initActionBar() {}
}
