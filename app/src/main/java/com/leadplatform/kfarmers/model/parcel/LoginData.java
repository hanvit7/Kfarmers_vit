package com.leadplatform.kfarmers.model.parcel;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginData implements Parcelable {
	public static final String KEY = "LoginData";

	private String id;
	private String pw;
	private boolean bAuto;

	public LoginData() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public boolean isbAuto() {
		return bAuto;
	}

	public void setbAuto(boolean bAuto) {
		this.bAuto = bAuto;
	}

	/***************************************************************/
	// Parcelable
	/***************************************************************/
	public LoginData(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(pw);
		dest.writeInt(bAuto ? 1 : 0);
	}

	private void readFromParcel(Parcel in) {
		this.id = in.readString();
		this.pw = in.readString();
		this.bAuto = (in.readInt() == 1) ? true : false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<LoginData> CREATOR = new Parcelable.Creator<LoginData>() {

		@Override
		public LoginData createFromParcel(Parcel source) {

			return new LoginData(source);

		}

		@Override
		public LoginData[] newArray(int size) {

			return new LoginData[size];

		}

	};
}
