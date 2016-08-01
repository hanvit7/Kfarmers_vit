package com.leadplatform.kfarmers.model.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.leadplatform.kfarmers.util.CommonUtil;

public class EditGeneralData implements Parcelable {
	public static final String KEY = "EditGeneralData";

	private String profile;
	private String phone;
	private String email;
	private boolean emailFlag;
	private boolean phoneFlag;
	private String userOldPw;
	private String userNewPW;
	private String name;

	public EditGeneralData() {

	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getPhone() {
		return phone;
	}

	public String getConvertPhone() {
		return CommonUtil.PatternUtil.convertPhoneFormat(phone);
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(boolean emailFlag) {
		this.emailFlag = emailFlag;
	}

	public boolean isPhoneFlag() {
		return phoneFlag;
	}

	public void setPhoneFlag(boolean phoneFlag) {
		this.phoneFlag = phoneFlag;
	}

	public String getUserOldPw() {
		return userOldPw;
	}

	public void setUserOldPw(String userOldPw) {
		this.userOldPw = userOldPw;
	}

	public String getUserNewPW() {
		return userNewPW;
	}

	public void setUserNewPW(String userNewPW) {
		this.userNewPW = userNewPW;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/***************************************************************/
	// Parcelable
	/***************************************************************/
	public EditGeneralData(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(profile);
		dest.writeString(phone);
		dest.writeString(email);
		dest.writeInt(emailFlag ? 1 : 0);
		dest.writeInt(phoneFlag ? 1 : 0);
		dest.writeString(userOldPw);
		dest.writeString(userNewPW);
		dest.writeString(name);
	}

	private void readFromParcel(Parcel in) {
		this.profile = in.readString();
		this.phone = in.readString();
		this.email = in.readString();
		this.emailFlag = (in.readInt() == 1) ? true : false;
		this.phoneFlag = (in.readInt() == 1) ? true : false;
		this.userOldPw = in.readString();
		this.userNewPW = in.readString();
		this.name = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<EditGeneralData> CREATOR = new Parcelable.Creator<EditGeneralData>() {

		@Override
		public EditGeneralData createFromParcel(Parcel source) {

			return new EditGeneralData(source);

		}

		@Override
		public EditGeneralData[] newArray(int size) {

			return new EditGeneralData[size];

		}

	};
}
