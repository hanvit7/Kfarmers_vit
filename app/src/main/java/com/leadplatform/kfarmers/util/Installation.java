package com.leadplatform.kfarmers.util;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class Installation {
	private static String sID = null;
	private static final String INSTALLATION = "INSTALLATION";

	public synchronized static String id(Context context) {
		if (sID == null) {
			File installation = new File(context.getFilesDir(), INSTALLATION);
			try {
				if (!installation.exists())
				{
					writeInstallationFile(installation,context);
				}
				sID = readInstallationFile(installation);
				Log.d("uuid",sID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return sID;
	}

	private static String readInstallationFile(File installation)
			throws IOException {
		RandomAccessFile f = new RandomAccessFile(installation, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installation,Context context) throws IOException 
	{
		FileOutputStream out = new FileOutputStream(installation);
		
		String uid = "";
		String androidID = "";
		String phoneIMEI = "";
		String serial = ""; 

		try
        {
			androidID = ""+Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);  
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }

        try 
        {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) 
            {
                phoneIMEI = "" + tm.getDeviceId();
                serial = "" + tm.getSimSerialNumber();
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        if (!androidID.equals("") || !phoneIMEI.equals("") || !serial.equals("")) 
	    {
        	uid = new UUID(androidID.hashCode(),(long)phoneIMEI.hashCode() << 32 | serial.hashCode()).toString()+"";
        }
        
        if(uid.equals(""))
        {
        	uid = UUID.randomUUID().toString();
        }
        
        out.write(uid.getBytes());
		out.close();
	}
}

