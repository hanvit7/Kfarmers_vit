package com.leadplatform.kfarmers.util.gcm;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.model.database.InquiryDb;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.WakeUpScreen;
import com.leadplatform.kfarmers.view.common.GcmComentActivity;
import com.leadplatform.kfarmers.view.common.ShopActivity;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.Supporters.SupportersDetailActivity;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.leadplatform.kfarmers.view.menu.notice.FarmNoticeActivity;
import com.leadplatform.kfarmers.view.menu.notice.TypeNoticeActivity;
import com.leadplatform.kfarmers.view.menu.order.OrderGeneralActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.leadplatform.kfarmers.view.recipe.RecipeViewActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class GcmIntentService extends IntentService {
	
	public static final String ACTION_TYPE = "actionType";
	public static final String PUSH_ID = "Id";
	public static final String PUSH_BUNDLE = "data";
    public static final String PUSH_TYPE = "GcmType";
    public static final String PUSH_INDEX = "Index";
    public static final String PUSH_SUBINDEX = "SubIndex";
    public static final String PUSH_TITLE = "title";
    public static final String PUSH_MSG = "msg";
    public static final String PUSH_IMG = "image_url";
    public static final String PUSH_COMMONT_BOARD_TYPE = "BoardType";
    
    
    private static int NOTIFICATION_ID = 4947;
	

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

 		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM will be extended in the future with new message types, just ignore any message types you're not interested in, or that
			 * you don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				//sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				//sendNotification("Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				
				if(null != intent.getStringExtra(PUSH_TYPE) && !intent.getStringExtra(PUSH_TYPE).isEmpty())
				{
					final HashMap<String, String> hashMap = new HashMap<String, String>();
					
					hashMap.put(PUSH_TYPE, intent.getStringExtra(PUSH_TYPE));
					
					if(null != intent.getStringExtra(PUSH_INDEX) && !intent.getStringExtra(PUSH_INDEX).isEmpty())
						hashMap.put(PUSH_INDEX, intent.getStringExtra(PUSH_INDEX));
					
					if(null != intent.getStringExtra(PUSH_SUBINDEX) && !intent.getStringExtra(PUSH_SUBINDEX).isEmpty())
						hashMap.put(PUSH_SUBINDEX, intent.getStringExtra(PUSH_SUBINDEX));
					else
						hashMap.put(PUSH_SUBINDEX, "");
					
					if(null != intent.getStringExtra(PUSH_TITLE) && !intent.getStringExtra(PUSH_TITLE).isEmpty())
						hashMap.put(PUSH_TITLE, intent.getStringExtra(PUSH_TITLE));
					else
						hashMap.put(PUSH_TITLE, "");
					if(null != intent.getStringExtra(PUSH_MSG) && !intent.getStringExtra(PUSH_MSG).isEmpty())
						hashMap.put(PUSH_MSG, intent.getStringExtra(PUSH_MSG));
					else
						hashMap.put(PUSH_MSG, "");
					
					if(null != intent.getStringExtra(PUSH_IMG) && !intent.getStringExtra(PUSH_IMG).isEmpty())
						hashMap.put(PUSH_IMG, intent.getStringExtra(PUSH_IMG));
					else
						hashMap.put(PUSH_IMG, "");
					
					if(null != intent.getStringExtra(PUSH_COMMONT_BOARD_TYPE) && !intent.getStringExtra(PUSH_COMMONT_BOARD_TYPE).isEmpty())
						hashMap.put(PUSH_COMMONT_BOARD_TYPE, intent.getStringExtra(PUSH_COMMONT_BOARD_TYPE));
					else
						hashMap.put(PUSH_COMMONT_BOARD_TYPE, "");
					
					hashMap.put(ACTION_TYPE, "push");

					if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("INQUIRE"))
					{
						Intent inetntChat = new Intent();
						inetntChat.putExtra("idx",hashMap.get(GcmIntentService.PUSH_INDEX));
						inetntChat.setAction("com.leadplatform.kfarmers.view.inquiry.chat");

						sendOrderedBroadcast(inetntChat, null, new BroadcastReceiver() {
							@Override
							public void onReceive(Context context, Intent intent) {
								Bundle results = getResultExtras(true);

								String check = results.getString("result");

								if (check == null) {
									check = "false";
								}

								InquiryDb queryInquiryDb = DbController.queryInquiry(getApplicationContext(), hashMap.get(GcmIntentService.PUSH_INDEX));
								InquiryDb inquiryDb = new InquiryDb();
								inquiryDb.idx = hashMap.get(GcmIntentService.PUSH_INDEX);

								if (!check.equals("true")) {
									inquiryDb.read = "F";
									noticNotification(hashMap);
								} else {
									inquiryDb.read = "T";
								}

								if (queryInquiryDb == null) {
									DbController.insertInquiry(getApplicationContext(), inquiryDb);
								} else {
									DbController.updateInquiry(getApplicationContext(), inquiryDb);
								}
								Intent intentCount = new Intent();
								intentCount.setAction("com.leadplatform.kfarmers.view.main.chatcount");
								sendBroadcast(intentCount);

							}
						}, null, Activity.RESULT_OK, null, null);


						/*ContentValues cv = new ContentValues();
						cv.put("idx", hashMap.get(GcmIntentService.PUSH_INDEX));
						getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI + InquiryProvider.INQUIRY_INSERT), cv);

						TimerTask sensorTask = new TimerTask() {
							@Override
							public void run() {
								ContentValues cv = new ContentValues();
								cv.put("show","false");
								Uri uri = getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI + InquiryProvider.INQUIRY_CHECK), cv);
								List<String> urlData = uri.getPathSegments();
								String check = urlData.get(0);

								InquiryDb queryInquiryDb = DbController.queryInquiry(getApplicationContext(), hashMap.get(GcmIntentService.PUSH_INDEX));

								InquiryDb inquiryDb = new InquiryDb();
								inquiryDb.idx = hashMap.get(GcmIntentService.PUSH_INDEX);

								if (!check.equals("true")) {
									inquiryDb.read = "F";
									noticNotification(hashMap);
								}
								else
								{
									inquiryDb.read = "T";
								}

								if(queryInquiryDb == null)
								{
									DbController.insertInquiry(getApplicationContext(), inquiryDb);
								}
								else
								{
									DbController.updateInquiry(getApplicationContext(),inquiryDb);
								}
								Intent intent = new Intent();
								intent.setAction("com.leadplatform.kfarmers.view.main.chatcount");
								sendBroadcast(intent);
							}
						};
						Timer sTimer = new Timer();
						sTimer.schedule(sensorTask, 500);*/
					} else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("MULTINOTICE")) {
						if(hashMap.get(PUSH_COMMONT_BOARD_TYPE).equals("G")) {
							noticNotification(hashMap);
						} else {
							if (AppPreferences.getLogin(this)) {
								String mProfile = DbController.queryProfileContent(getApplicationContext());
								try {
									JsonNode root = JsonUtil.parseTree(mProfile);
									String type = root.findValue("Type").textValue();
									if(!type.equals("U")) {
										noticNotification(hashMap);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					else
					{
						String pushData = hashMap.toString();

						ArrayList<String> data = AppPreferences.getPushData(getApplicationContext());

						boolean isNewData = true;

						for(String str : data)
						{
							if(pushData.equals(str))
							{
								isNewData = false;
								break;
							}
						}
						if(isNewData)
						{
							noticNotification(hashMap);
						}
						AppPreferences.setPushData(getApplicationContext(), pushData);
					}
					
					/*if(isNewData)
					{
						if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("INQUIRE"))
						{
							ContentValues cv = new ContentValues();
							cv.put("idx", hashMap.get(GcmIntentService.PUSH_INDEX));
							getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI + InquiryProvider.INQUIRY_INSERT), cv);

							TimerTask sensorTask = new TimerTask() {
								@Override
								public void run() {
									ContentValues cv = new ContentValues();
									cv.put("show","false");
									Uri uri = getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI + InquiryProvider.INQUIRY_CHECK), cv);
									List<String> urlData = uri.getPathSegments();
									String check = urlData.get(0);
									if (!check.equals("true")) {
										noticNotification(hashMap);
									}
								}
							};
							Timer sTimer = new Timer();
							sTimer.schedule(sensorTask, 500);


							*//*new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									ContentValues cv = new ContentValues();
									cv.put("show","false");
									Uri uri = getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI + InquiryProvider.INQUIRY_CHECK), cv);
									List<String> urlData = uri.getPathSegments();
									String check = urlData.get(0);
									if (!check.equals("true")) {
										noticNotification(hashMap);
									}
								}
							}, 10);*//*
						}
						else
						{
							noticNotification(hashMap);
						}
						*//*if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENT") || hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENTNOTICE"))
				    	{
							if(CommonUtil.isScreenOn(getApplicationContext()))
							{
								PopUpNotification(hashMap);
								//noticNotification(hashMap);	
							}
							else
							{
								PopUpNotification(hashMap);
								//noticNotification(hashMap);
							}
				    	}
						else
						{
							noticNotification(hashMap);	
						}*//*
						AppPreferences.setPushData(getApplicationContext(), pushData);
					}*/
				}
				
/*				String GcmType = intent.getStringExtra("GcmType");
				if (GcmType.equals("COMMENT")) {
					String replyType = intent.getStringExtra("BoardType");
					String diaryTitle = intent.getStringExtra("title");
					String diaryIndex = intent.getStringExtra("Index");
					String msg = intent.getStringExtra("msg");

					commentNotification(replyType, diaryTitle, diaryIndex, msg);
				}
*/
	
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void PopUpNotification(HashMap<String, String> hashMap)
	{
		getApplicationContext().startActivity( new Intent(getApplicationContext(), GcmComentActivity.class).putExtra(PUSH_BUNDLE, hashMap).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
	}
	
	private void noticNotification(HashMap<String, String> hashMap) {

		int id = (int) (NOTIFICATION_ID + System.currentTimeMillis());
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Bitmap bitmap = null;
		
		if(!PatternUtil.isEmpty(hashMap.get(PUSH_IMG)))
			bitmap = ImageLoader.getInstance().loadImageSync(hashMap.get(PUSH_IMG));

		Intent intent = new Intent(getApplicationContext(), StartActivityForPush.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		intent.putExtra(PUSH_BUNDLE,hashMap);

		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder;

		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		boolean isNight = false;
		if(hour <=7 || hour >= 22)
		{
			isNight = true;
		}
		else
		{
			isNight = false;
		}
		
		if(null!=bitmap)
		{
			if(isNight)
			{
				mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon_launcher).setContentTitle(hashMap.get(PUSH_TITLE)).setContentText(hashMap.get(PUSH_MSG)).setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_LIGHTS).setVibrate(new long[]{0l}).setContentIntent(contentIntent).setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setBigContentTitle(hashMap.get(PUSH_TITLE)).setSummaryText(hashMap.get(PUSH_MSG))).setPriority(NotificationCompat.PRIORITY_MAX).setTicker(hashMap.get(PUSH_TITLE));
			}
			else
			{
				mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon_launcher).setContentTitle(hashMap.get(PUSH_TITLE)).setContentText(hashMap.get(PUSH_MSG)).setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_SOUND).setLights(Color.GREEN, 500, 500).setVibrate(new long[] {0,1000}).setContentIntent(contentIntent).setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).setBigContentTitle(hashMap.get(PUSH_TITLE)).setSummaryText(hashMap.get(PUSH_MSG))).setPriority(NotificationCompat.PRIORITY_MAX).setTicker(hashMap.get(PUSH_TITLE));
			}
		}
		else
		{
			if(isNight)
			{
				mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon_launcher).setContentTitle(hashMap.get(PUSH_TITLE)).setContentText(hashMap.get(PUSH_MSG)).setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_LIGHTS).setVibrate(new long[]{0l}).setContentIntent(contentIntent).setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(hashMap.get(PUSH_TITLE)).bigText(hashMap.get(PUSH_MSG))).setPriority(NotificationCompat.PRIORITY_MAX).setTicker(hashMap.get(PUSH_TITLE));
			}
			else
			{
				mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.icon_launcher).setContentTitle(hashMap.get(PUSH_TITLE)).setContentText(hashMap.get(PUSH_MSG)).setAutoCancel(true)
						.setDefaults(Notification.DEFAULT_SOUND).setLights(Color.GREEN, 500, 500).setVibrate(new long[]{0, 1000}).setContentIntent(contentIntent).setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(hashMap.get(PUSH_TITLE)).bigText(hashMap.get(PUSH_MSG))).setPriority(NotificationCompat.PRIORITY_MAX).setTicker(hashMap.get(PUSH_TITLE));
			}
		}
		mNotificationManager.notify(id, mBuilder.build());
		WakeUpScreen.acquire(getApplicationContext(), 5000);
	}
	
	 public static void separatePushNotice(Context context,HashMap<String, String> hashMap) {

        if (hashMap == null) {
            return;
        }
        
        AppPreferences.setPushAllData(context, new ArrayList<String>());
        
        Intent intent = null;
        
    	if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("FarmerShopProduct"))
    	{
			intent = new Intent(context, ProductActivity.class);
			intent.putExtra("productIndex", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	}
    	else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("Diary"))
    	{
			intent = new Intent(context, DiaryDetailActivity.class);
			intent.putExtra("diary", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	}
    	else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENT") || hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENTREVIEWS"))
    	{
    		intent = new Intent(context, ReplyActivity.class);
    		
    		String replyType = hashMap.get(GcmIntentService.PUSH_COMMONT_BOARD_TYPE);

			if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("COMMENTREVIEWS"))
			{
				intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_REVIEW);
			}
			else
			{
				if (replyType.equals("F"))
					intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_FARMER);
				else if (replyType.equals("V"))
					intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_VILLAGE);
				else if (replyType.equals("C"))
					intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
				else if (replyType.equals("D"))
					intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
				else if (replyType.equals("N"))
					intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
			}

			intent.putExtra("diaryTitle", hashMap.get(GcmIntentService.PUSH_TITLE));
			intent.putExtra("diaryIndex", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	}
		
    	else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("Farm"))
		{
	        intent = new Intent(context, FarmActivity.class);
			intent.putExtra("userIndex", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.putExtra("userType", "F");
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("INQUIRE"))
		{
			intent = new Intent(context, InquiryActivity.class);
			intent.putExtra("index", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("MULTINOTICE"))
		{
			if(hashMap.get(PUSH_COMMONT_BOARD_TYPE).equals("G")) {
				intent = new Intent(context, FarmNoticeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("userType", "G");
				intent.putExtra("userIndex", "");
				if (hashMap.get(PUSH_INDEX) != null && !hashMap.get(PUSH_INDEX).isEmpty()) {
					intent.putExtra("noticeIndex", hashMap.get(PUSH_INDEX));
				} else {
					intent.putExtra("noticeIndex", "");
				}
			} else {
				intent = new Intent(context, TypeNoticeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("noticeType", "F");
				if (hashMap.get(PUSH_INDEX) != null && !hashMap.get(PUSH_INDEX).isEmpty()) {
					intent.putExtra("noticeIndex", hashMap.get(PUSH_INDEX));
				} else {
					intent.putExtra("noticeIndex", "");
				}
			}
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("Event"))
		{
			intent = new Intent(context, SupportersDetailActivity.class);
			intent.putExtra(SupportersDetailActivity.EVENT_IDX, hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.putExtra("index", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("Exhibition"))
		{
			intent = new Intent(context, ShopActivity.class);
			intent.putExtra("id", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.putExtra("type",ShopActivity.type.Plan);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("Recipe"))
		{
			intent = new Intent(context, RecipeViewActivity.class);
			intent.putExtra("recipe", hashMap.get(GcmIntentService.PUSH_INDEX));
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else if(hashMap.get(GcmIntentService.PUSH_TYPE).equals("GONGGUORDER"))
		{
			intent = new Intent(context, OrderGeneralActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}

		else
    	{
			intent = new Intent(context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	}
    	context.startActivity(intent);
	}

	private void commentNotification(String replyType, String diaryTitle, String diaryIndex, String msg) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(getApplicationContext(), ReplyActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (replyType.equals("F"))
			intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_FARMER);
		else if (replyType.equals("V"))
			intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_VILLAGE);
		else if (replyType.equals("C"))
			intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
		else if (replyType.equals("D"))
			intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
		else if (replyType.equals("N"))
			intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_NORMAL);
		// else if(replyType.equals("T"))
		// intent.putExtra("replyType", ReplyActivity.REPLY_TYPE_FARMER);

		intent.putExtra("diaryTitle", diaryTitle);
		intent.putExtra("diaryIndex", diaryIndex);

		PendingIntent contentIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon_launcher).setContentTitle("kFarmers").setContentText(msg).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE).setContentIntent(contentIntent);
		
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("msg", msg);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Bitmap bitmap = ImageLoader.getInstance().loadImageSync(msg);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon_launcher).setContentTitle("kFarmers")
				.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)).setContentText(msg)
				// .setContentTitle("GCM Notification").setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg)
				.setAutoCancel(true).setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
		// .setVibrate(new long[] { 0, 500 });

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	public static boolean isScreenOn(Context context) 
	{
		return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
	}
}
