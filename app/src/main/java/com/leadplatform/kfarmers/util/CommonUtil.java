package com.leadplatform.kfarmers.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.*;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class CommonUtil {
	public static class SystemUtil {
		public static long currentTimeMillis() {
			return System.currentTimeMillis();
		}

		public static void arraycopy(Object src, int srcPos, Object dst, int dstPos, int length) {
			try {
				System.arraycopy(src, srcPos, dst, dstPos, length);
			} catch (Exception e) {

			}
		}

		public static void sleep(long millis) {
			SystemClock.sleep(millis);
		}

		public static void gc() {
			System.gc();
		}

		public static void sendLOGEmail(Context context, String strPath) {
			Intent intent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
			intent.setType("plain/text");
			ArrayList<Uri> uris = new ArrayList<Uri>();

			String strDir = Environment.getExternalStorageDirectory() + "/" + strPath;
			File directory = new File(strDir);
			if (directory.isDirectory()) {
				File[] files = directory.listFiles();
				for (File file : files) {
					Uri u = Uri.fromFile(file);
					uris.add(u);
				}
			} else {
				uris.add(Uri.fromFile(directory));
			}

			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			context.startActivity(intent);
		}
	}

	public static class FileUtil {
		public static void createDirectory(String directory) {
			File path = new File(directory);

			if (path.isDirectory() == false) {
				try {
					path.mkdirs();
				} catch (Exception e) {

				}
			}
			path = null;
		}

		public static boolean isFileExist(String name) {
			if (name == null || name.length() < 0)
				return false;

			boolean bRet = false;
			File file = new File(name);

			bRet = file.isFile();
			file = null;

			return bRet;
		}

		public static boolean deleteFile(String name) {
			boolean bRet = false;

			if (name == null || name.length() < 0 || isFileExist(name) == false)
				return bRet;

			File file = new File(name);
			bRet = file.delete();
			file = null;
			return bRet;
		}

		public static boolean deleteSerializeFile(String path, String name) {
			boolean bRet = false;

			if (path == null || path.length() < 0)
				return bRet;
			if (name == null || name.length() < 0)
				return bRet;

			if (isSerializeFileExist(path, name) == false)
				return bRet;

			File file = new File(path, name);
			bRet = file.delete();
			file = null;
			return bRet;
		}

		private static boolean isSerializeFileExist(String path, String name) {
			boolean bRet = false;
			File filePath = new File(path, name);

			bRet = filePath.isFile();
			filePath = null;

			return bRet;
		}

		public static Object loadSerializeFile(Context context, String fileName) {
			if (context == null || fileName == null || fileName.length() < 0)
				return null;

			FileInputStream fis = null;
			ObjectInputStream ois = null;
			Object outputObj = null;

			try {
				fis = context.openFileInput(fileName);
				ois = new ObjectInputStream(fis);
				outputObj = ois.readObject();

				fis.close();
				ois.close();
			} catch (Exception e) {
				outputObj = null;
			}
			ois = null;
			return outputObj;
		}

		public static boolean saveSerializeFile(Context context, String fileName, Object object) {
			if (context == null || fileName == null || fileName.length() < 0)
				return false;

			FileOutputStream fos = null;
			ObjectOutputStream oos = null;

			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);

				oos.writeObject(object);

				fos.close();
				oos.close();
				oos = null;
				return true;

			} catch (FileNotFoundException e1) {

			} catch (IOException e) {

			}

			oos = null;
			return false;
		}

		public static long getFolderSize(File file) {
	        if (!file.exists() || !file.isDirectory()) return 0;

	        long size = 0;
	        File[] children = file.listFiles();
	        for (int i = 0; i < children.length; i++) {
	            if (children[i].isFile())
	                size = size + children[i].length();
	            else
	                size = size + getFolderSize(children[i]);
	        }
	        return size;
	    }
		
		public static void delFolderFile(File file) {
	        if (!file.exists() || !file.isDirectory()) return;
	        
	        File[] children = file.listFiles();
	        for (int i = 0; i < children.length; i++) {
	            if (children[i].isFile() && children[i].getName().split("\\.").length>1)
	                children[i].delete();
	            else
	            	delFolderFile(children[i]);
	        }
	    }

		public static void getFolderList(File file) {
	        if (!file.exists() || !file.isDirectory()) return; 

	        File[] children = file.listFiles();
	        for (int i = 0; i < children.length; i++) {
	            if (children[i].isFile()) System.out.println(children[i].getAbsolutePath() + ", " + children[i].length());
	            else getFolderList(children[i]);
	        }
	    }		
	}

	public static class AndroidUtil {
		public static boolean isVibrateMode(Context context) {
			AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT || mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				return true;
			}
			return false;
		}

		public static boolean isGpsProviderEnabled(Context context) {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				return true;
			}
			return false;
		}

		public static void setPendingAlarmManager(Intent intent, Context context, long triggerAtTime) {
			PendingIntent sender = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, sender);
		}

		public static void setPendingAlarmManager(Intent intent, Context context, int requestCode, long triggerAtTime) {
			PendingIntent sender = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, sender);
		}

		public static void cancelPendingAlarmManager(Intent intent, Context context) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}

		public static void cancelPendingAlarmManager(Intent intent, Context context, int requestCode) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}

		public static String getAppVersion(Context context) {
			String version = "";
			try {
				PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				version = packageInfo.versionName;
			} catch (Exception e) {
				return version;
			}
			return version;
		}

		public static String getDeviceName() {
			return Build.MODEL;
		}

		public static boolean enableUseNetwork(Context context) {
			ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}

			return false;
		}

		public static boolean isForeground(Context context) {
			boolean result = false;

			String packageName = context.getPackageName();

			RunningAppProcessInfo info = null;
			ActivityManager actMgr = null;

			if (actMgr == null) {
				actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			}

			List<RunningAppProcessInfo> l = actMgr.getRunningAppProcesses();
			Iterator<RunningAppProcessInfo> iter = l.iterator();
			while (iter.hasNext()) {
				info = iter.next();
				if (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					if (info.processName.equals(packageName)) {
						result = true;
						break;
					}
				}
			}

			return result;
		}

		/**
		 * ���� ���÷��� ȭ�鿡 ����� DP������ Pixel�� ��ȯ�Ͽ� ��ȯ.
		 * 
		 * @param context
		 * @param dp
		 *            dp��
		 * @return ��ȯ�� ��(pixel)
		 */
		public static int pixelFromDp(Activity activity, int dp) {
			DisplayMetrics outMetrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
			int density = outMetrics.densityDpi;

			return (int) (dp * ((float) density / 160));
		}

		/**
		 * �̹��������� ȸ�� ������ �����Ѵ�.
		 */
		public static int getImageFileDegree(String filePath) {
			int exifDegree = 0;
			try {
				// �̹����� ��Ȳ�� �°� ȸ���Ų��
				ExifInterface exif = new ExifInterface(filePath);
				int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				exifDegree = exifOrientationToDegrees(exifOrientation);
			} catch (Exception e) {

			}

			return exifDegree;
		}

		/**
		 * EXIF������ ȸ��� ��ȯ�Ͽ� �����Ѵ�
		 */
		private static int exifOrientationToDegrees(int exifOrientation) {
			if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
				return 90;
			} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
				return 180;
			} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
				return 270;
			}
			return 0;
		}

		public static String getPhoneNumber(Context context) {
			TelephonyManager systemService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String PhoneNumber = systemService.getLine1Number();
			if (PhoneNumber != null && PhoneNumber.length()>0) {
				PhoneNumber = PhoneNumber.substring(PhoneNumber.length() - 10, PhoneNumber.length());
				PhoneNumber = "0" + PhoneNumber;
			}
			return PhoneNumber;
		}

		public static void actionCall(Context context, String phoneNum) {
			context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum)));
		}

		public static void actionDial(Context context, String phoneNum) {
            try {
                context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNum)));
            }catch (Exception e)
            {
                Toast.makeText(context,"전화기능이 없는 기기 입니다.",Toast.LENGTH_SHORT).show();
            }
		}
		public static void actionSms(Context context, String phoneNum,String sms) {
			
			Uri uri = Uri.parse("smsto:"+phoneNum);    
			Intent itent = new Intent(Intent.ACTION_SENDTO, uri);    
			itent.putExtra("sms_body",sms);    
			context.startActivity(itent);
		}
	}

	public static class TimeUtil {
		public static String getHourMinuteFormat(String time) {
			SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
			DateFormatSymbols temp = new DateFormatSymbols();
			temp.setAmPmStrings(new String[] { "AM", "PM" });
			format.setDateFormatSymbols(temp);

			int hour = Integer.parseInt(time.substring(6, 8));
			int minute = Integer.parseInt(time.substring(8, 10));

			Date date = new Date();
			date.setHours(hour);
			date.setMinutes(minute);

			return format.format(date);
		}

		// 12byte string("10 09 31 11 50 45")
		public static String getDateFormat(String mDate) {
			SimpleDateFormat fomat = new SimpleDateFormat("MM.dd.yyyy hh:mm a");
			DateFormatSymbols temp = new DateFormatSymbols();
			temp.setAmPmStrings(new String[] { "AM", "PM" });
			fomat.setDateFormatSymbols(temp);

			int year = 0;
			int month = 0;
			int day = 0;
			int hour = 0;
			int min = 0;

			if (mDate.length() == 12) {
				year = Integer.parseInt("20" + mDate.substring(0, 2)) - 1900;
				month = Integer.parseInt(mDate.substring(2, 4));
				day = Integer.parseInt(mDate.substring(4, 6));
				hour = Integer.parseInt(mDate.substring(6, 8));
				min = Integer.parseInt(mDate.substring(8, 10));
			} else if (mDate.length() == 14) {
				year = Integer.parseInt(mDate.substring(0, 4)) - 1900;
				month = Integer.parseInt(mDate.substring(4, 6));
				day = Integer.parseInt(mDate.substring(6, 8));
				hour = Integer.parseInt(mDate.substring(8, 10));
				min = Integer.parseInt(mDate.substring(10, 12));
			}

			if (month > 0 && month < 13 && day > 0 && day < 32) {
				month -= 1;
				Date date = new Date(year, month, day, hour, min);

				return fomat.format(date);
			} else {
				char a = 'a';

				if ((hour - 12) >= 0 && hour < 24) {
					if (hour > 12)
						hour -= 12;
					a = 'p';
				} else {
					if (hour == 24)
						hour = 0;
				}

				return month + "." + day + "." + year + " " + hour + ":" + min + " " + a;
			}
		}

		public static String dateFormat4(String mDate) {
			SimpleDateFormat fomat = new SimpleDateFormat("MM.dd.yyyy");
			DateFormatSymbols temp = new DateFormatSymbols();
			fomat.setDateFormatSymbols(temp);

			int year = 0;
			int month = 0;
			int day = 0;
			int hour = 0;
			int min = 0;

			if (mDate.length() == 12) {
				year = Integer.parseInt("20" + mDate.substring(0, 2)) - 1900;
				month = Integer.parseInt(mDate.substring(2, 4));
				day = Integer.parseInt(mDate.substring(4, 6));
				hour = Integer.parseInt(mDate.substring(6, 8));
				min = Integer.parseInt(mDate.substring(8, 10));
			} else if (mDate.length() == 14) {
				year = Integer.parseInt(mDate.substring(0, 4)) - 1900;
				month = Integer.parseInt(mDate.substring(4, 6));
				day = Integer.parseInt(mDate.substring(6, 8));
				hour = Integer.parseInt(mDate.substring(8, 10));
				min = Integer.parseInt(mDate.substring(10, 12));
			}
			// Driving info ��¥ ������ ������ �� ��¥ ���� ǥ�� ����� ���� �߰�
			// �����(yyMMdd) ������ ����
			// added by LKJ@20110921
			else if (mDate.length() == 6) {
				year = Integer.parseInt("20" + mDate.substring(0, 2)) - 1900;
				month = Integer.parseInt(mDate.substring(2, 4));
				day = Integer.parseInt(mDate.substring(4, 6));
			}

			if (month > 0 && month < 13 && day > 0 && day < 32) {
				month -= 1;
				Date date = new Date(year, month, day, hour, min);

				return fomat.format(date);
			} else {
				return month + "." + day + "." + year;
			}
		}

		public static String dateFormat5(String mDate) {
			SimpleDateFormat fomat = new SimpleDateFormat("MM.dd.yyyy  hh:mm a");
			DateFormatSymbols temp = new DateFormatSymbols();
			temp.setAmPmStrings(new String[] { "AM", "PM" });
			fomat.setDateFormatSymbols(temp);

			int year = 0;
			int month = 0;
			int day = 0;
			int hour = 0;
			int min = 0;

			if (mDate.length() == 12) {
				year = Integer.parseInt("20" + mDate.substring(0, 2)) - 1900;
				month = Integer.parseInt(mDate.substring(2, 4));
				day = Integer.parseInt(mDate.substring(4, 6));
				hour = Integer.parseInt(mDate.substring(6, 8));
				min = Integer.parseInt(mDate.substring(8, 10));
			} else if (mDate.length() == 14) {
				year = Integer.parseInt(mDate.substring(0, 4)) - 1900;
				month = Integer.parseInt(mDate.substring(4, 6));
				day = Integer.parseInt(mDate.substring(6, 8));
				hour = Integer.parseInt(mDate.substring(8, 10));
				min = Integer.parseInt(mDate.substring(10, 12));
			}

			if (month > 0 && month < 13 && day > 0 && day < 32) {
				month -= 1;
				Date date = new Date(year, month, day, hour, min);

				return fomat.format(date);
			} else {
				String a = "AM";

				if ((hour - 12) >= 0 && hour < 24) {
					if (hour > 12)
						hour -= 12;
					a = "PM";
				} else {
					if (hour == 24)
						hour = 0;
				}

				return month + "." + day + "." + year + "  " + hour + ":" + min + " " + a;
			}
		}

		public static int getTimeSecond(int hour, int minute, int second) {
			double secondTime = 0;

			if (hour != 0 && hour > 0)
				secondTime = hour * (60 * 60);

			if (minute != 0 && minute > 0)
				secondTime = secondTime + (minute * 60);

			if (second != 0 && second > 0)
				secondTime = secondTime + (second);

			return (int) Math.floor(secondTime);
		}

		public static int getHour(int second) {
			double hourTime = 0;

			if (second == 0 || second < 0)
				return 0;

			hourTime = second / 60 / 60;

			return (int) Math.floor(hourTime);
		}

		public static int getMinute(int second) {
			double minuteTime = 0;

			if (second == 0 || second < 0)
				return 0;

			double hourTime = second / 60 / 60;
			minuteTime = (second - (60 * 60) * hourTime) / 60;
			return (int) Math.floor(minuteTime);
		}

		public static int getSecond(int second) {
			double secondTime = 0;

			if (second == 0 || second < 0)
				return 0;

			double hourTime = second / 60 / 60;
			double minuteTime = (second - ((60 * 60) * hourTime)) / 60;

			secondTime = second - ((60 * 60) * hourTime) - (minuteTime * 60);

			return (int) Math.floor(secondTime);
		}

		public static Calendar simpleDateFormat(String sDate) {
			Date date = null;
			Calendar c = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = format.parse(sDate);
				c.setTime(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return c;
		}

		public static String simpleDateFormat(Date date) {
			String sDate = null;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			try {
				sDate = format.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return sDate;
		}
		
		// Local Time -> UTC/GMT Time
		public static long convertLocalTimeToUTC(long pv_localDateTime)
		{
		    long lv_UTCTime = pv_localDateTime;
		    
		    TimeZone z = TimeZone.getDefault();
		    //int offset = z.getRawOffset(); // The offset not includes daylight savings time
		    int offset = z.getOffset(pv_localDateTime); // The offset includes daylight savings time
		    lv_UTCTime = pv_localDateTime - offset;
		    return lv_UTCTime;
		}

		// UTC/GMT Time -> Local Time
		public static long convertUTCToLocalTime(long pv_UTCDateTime)
		{
		    long lv_localDateTime = pv_UTCDateTime;
		    
		    TimeZone z = TimeZone.getDefault();
		    //int offset = z.getRawOffset(); // The offset not includes daylight savings time
		    int offset = z.getOffset(pv_UTCDateTime); // The offset includes daylight savings time
		    
		    lv_localDateTime = pv_UTCDateTime + offset;
		    
		    return lv_localDateTime;
		}

	}

	public static class BitmapUtil {
		private static int mThumnailSize = 200;

		/**
		 * ����� ���̳ʸ��� sampleSize �� ������. ��) ����� / sampleSize = ���.
		 */
		public static Bitmap getSampleSizeBitmap(byte[] data, int sampleSize) {
			Bitmap bitmap = null;

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Config.RGB_565;
			options.inSampleSize = sampleSize;

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

			return bitmap;
		}

		/**
		 * ������ ���̳ʸ� �����ͷ� �����Ѵ�.
		 */
		public static byte[] getImageFileData(String filePath) {
			FileInputStream fileInpushStream = null;
			byte[] data = null;

			if (filePath != null && filePath.length() > 0) {
				try {
					File file = new File(filePath);

					fileInpushStream = new FileInputStream(file);
					data = new byte[fileInpushStream.available()];

					while (fileInpushStream.read(data) != -1) {
                    }
					fileInpushStream.close();

				} catch (FileNotFoundException e) {
					data = null;
				} catch (IOException e) {
					data = null;
				}
			}
			return data;
		}

		/**
		 * ���簢�� �̹����� ���簢������ ����� mThumnailSize�� ���� �ʵ��� �����Ѵ�.
		 */
		public static Bitmap getSquareThumbnailBitmap(Bitmap bitmap) {
			Bitmap resized = null;

			int srcWidth = bitmap.getWidth();
			int srcHeight = bitmap.getHeight();

			int startWidth = 0, startHeight = 0;

			if (srcWidth > srcHeight) {
				startWidth = (srcWidth - srcHeight) / 2;
				srcWidth = srcHeight;
			} else {
				startHeight = (srcHeight - srcWidth) / 2;
				srcHeight = srcWidth;
			}

			Bitmap srcBitmap = Bitmap.createBitmap(bitmap, startWidth, startHeight, srcWidth, srcHeight);
			bitmap.recycle();

			if (startWidth < mThumnailSize || startHeight < mThumnailSize) {
				return srcBitmap;
			} else {
				resized = Bitmap.createScaledBitmap(srcBitmap, mThumnailSize, mThumnailSize, false);
				srcBitmap.recycle();

				return resized;
			}
		}

		/**
		 * �̹����� ȸ���Ͽ� ���� �Ѵ�.
		 */
		public static Bitmap rotate(Bitmap bitmap, int degrees) {
			if (degrees != 0 && bitmap != null) {
				Matrix m = new Matrix();
				m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

				try {

					Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
					if (bitmap != converted) {

						bitmap.recycle();
						bitmap = converted;
					}
				} catch (OutOfMemoryError ex) {
					// �޸𸮰� �����Ͽ� ȸ���� ��Ű�� ���� ��� �׳� ���� ��ȯ�Ѵ�.
				}
			}
			return bitmap;
		}
		
		public static boolean sameAs(Bitmap bitmap1, Bitmap bitmap2) {  
	         ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());  
	         bitmap1.copyPixelsToBuffer(buffer1);  
	   
	         ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());  
	         bitmap2.copyPixelsToBuffer(buffer2);  
	         return Arrays.equals(buffer1.array(), buffer2.array());  
		} 

	}

	public static class SimpleFormatUtil {
		public static String insertEndString(byte[] buff, int offset, int size) {
			byte[] temp = new byte[size];

			int i = 0;
			for (i = 0; i < size; i++) {
				if (buff[offset + i] == '\0') {
					temp[i] = '\n';
					break;
				} else {
					temp[i] = buff[offset + i];
				}
			}

			String sTemp = new String(buff, offset, size);
			sTemp = sTemp.substring(0, i);

			return sTemp;
		}

		/**
		 * ���ڸ� �Է� �޾Ƽ� 3�ڸ� ���� ","�� �������ش�.(�Ҽ��� ����)
		 * 
		 * @param unit
		 *            �Է¹��� ����
		 * @return ","���� ��
		 */
		public static String convertUnitToCommaUnit(long unit) {
			DecimalFormat df = new DecimalFormat("###,###");
			return df.format(unit);
		}

		/**
		 * �Ѿ�� double���� 3�ڸ� ����","�� ������ �ش�.(�Ҽ��� ����)
		 * 
		 * @param unit
		 *            �Է¹��� ����
		 * @param decimalPoint
		 *            �Ҽ��� ǥ���� �ڸ���
		 * @return
		 */
		public static String convertUnitToDecimalPointCommaUnit(double unit, int decimalPoint) {
			DecimalFormat df = null;

			switch (decimalPoint) {
			case 1:
				df = new DecimalFormat("###,###.#");
				break;
			case 2:
				df = new DecimalFormat("###,###.##");
				break;
			case 3:
				df = new DecimalFormat("###,###.###");
				break;
			case 4:
				df = new DecimalFormat("###,###.####");
				break;
			default:
				df = new DecimalFormat("###,###.#");
				break;
			}

			return df.format(unit);
		}

		/**
		 * m�� �Ѿ�� ���� km�� ��ȯ�Ѵ�.
		 * 
		 * @param m
		 * @return
		 */
		public static double convertMeterToKmUnit(long m) {
			return m * 0.001;
		}

		public static float convertMeterToKmUnitFloat(long m) {
			return m * 0.001f;
		}

		public static String removeComma(String source) {
			int commaIdx = 0;

			while ((commaIdx = source.indexOf(",")) != -1) {
				source = source.substring(0, commaIdx) + source.substring(commaIdx + 1);
			}
			return source;
		}
	}

	public static class UiUtil {
		public static void hideSoftKeyboard(Context context, View view) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}

		public static void showSoftKeyboard(Context context, View view) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
		}

		public static void unbindDrawables(View view) {
			if (view == null)
				return;

			if (view.getBackground() != null) {
				view.getBackground().setCallback(null);
			}

			if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				((ViewGroup) view).removeAllViews();
			}
		}
	}

	public static class PatternUtil {
		public static boolean isEmpty(String input) {
			if (input != null && input.length() != 0) {
				return false;
			}
			return true;
		}

		public static boolean isValidId(String input) {
			final String regex = "^[a-z]{1}[a-z0-9]{3,19}$";
			return Pattern.matches(regex, input);
		}

		public static boolean isValidPw(String input) {
			// final String regex = "^[a-zA-Z0-9]{4,20}$";
			// return Pattern.matches(regex, input);
			return true;
		}

		public static boolean isValidEmail(String input) {
			final String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
			return Pattern.matches(regex, input);
		}

		public static boolean isValidPhone(String input) {
			 final String regex = "^01(?:0|1[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
			 return Pattern.matches(regex, input);
			//return true;
		}

		public static boolean isValidName(String input) {
			if (input.length() >= 2 && input.length() <= 20) {
				return true;
			}
			return false;
		}

		public static String convertPhoneFormat(String phoneNo) {
			if (phoneNo == null || phoneNo.length() == 0) {
				return phoneNo;
			}

			String strTel = phoneNo;
			String[] DDD = { "02", "031", "032", "033", "041", "042", "043", "051", "052", "053", "054", "055", "061", "062", "063", "064", "010", "011", "012", "013", "015", "016", "017", "018",
					"019", "070" };

			if (strTel.length() < 9) {
				return strTel;
			} else if (strTel.substring(0, 2).equals(DDD[0])) {
				strTel = strTel.substring(0, 2) + '-' + strTel.substring(2, strTel.length() - 4) + '-' + strTel.substring(strTel.length() - 4, strTel.length());
			} else {
				for (int i = 1; i < DDD.length; i++) {
					if (strTel.substring(0, 3).equals(DDD[i])) {
						strTel = strTel.substring(0, 3) + '-' + strTel.substring(3, strTel.length() - 4) + '-' + strTel.substring(strTel.length() - 4, strTel.length());
					}
				}
			}
			return strTel;
		}
	}
	
	public static boolean isScreenOn(Context context) 
	{
		return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
	}
	
	public static void setVibratorAndRing(Context context) 
	{
        Vibrator mVibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        mVibe.vibrate(1000);
        
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
	}

	public static class DIPManager {
		public static int dip2px(int dip, Context context) {
			int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
			return px;
		}

		public static int px2dip(int px, Context context) {
			int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
			return dip;
		}
	}
}
