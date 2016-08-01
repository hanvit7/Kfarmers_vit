package com.leadplatform.kfarmers.model.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.leadplatform.kfarmers.util.CommonUtil;

public class JoinFarmerData implements Parcelable {
	public static final String KEY = "JoinFarmerData";

	private String profile;
	private String name;
	private String phone;
	private String email;
	private boolean emailFlag;
	private boolean phoneFlag;
	private String userID;
	private String userPW;
	private String confirmPw;
	private String farm;
	private String address;
	private String[] imagePath;
	private String category;
	private String introduction;
	private boolean duplicateFlag;
	private boolean agreeFlag;

	public JoinFarmerData() {

	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserPW() {
		return userPW;
	}

	public void setUserPW(String userPW) {
		this.userPW = userPW;
	}

	public String getConfirmPw() {
		return confirmPw;
	}

	public void setConfirmPw(String confirmPw) {
		this.confirmPw = confirmPw;
	}

	public String getFarm() {
		return farm;
	}

	public void setFarm(String farm) {
		this.farm = farm;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String[] getImagePath() {
		return imagePath;
	}

	public void setImagePath(String[] imagePath) {
		this.imagePath = imagePath;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public boolean isDuplicateFlag() {
		return duplicateFlag;
	}

	public void setDuplicateFlag(boolean duplicateFlag) {
		this.duplicateFlag = duplicateFlag;
	}

	public boolean isAgreeFlag() {
		return agreeFlag;
	}

	public void setAgreeFlag(boolean agreeFlag) {
		this.agreeFlag = agreeFlag;
	}

	/***************************************************************/
	// Parcelable
	/***************************************************************/
	public JoinFarmerData(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(profile);
		dest.writeString(name);
		dest.writeString(phone);
		dest.writeString(email);
		dest.writeInt(emailFlag ? 1 : 0);
		dest.writeInt(phoneFlag ? 1 : 0);
		dest.writeString(userID);
		dest.writeString(userPW);
		dest.writeString(confirmPw);
		dest.writeString(farm);
		dest.writeString(address);
		dest.writeStringArray(imagePath);
		dest.writeString(category);
		dest.writeString(introduction);
		dest.writeInt(duplicateFlag ? 1 : 0);
		dest.writeInt(agreeFlag ? 1 : 0);
	}

	private void readFromParcel(Parcel in) {
		this.profile = in.readString();
		this.name = in.readString();
		this.phone = in.readString();
		this.email = in.readString();
		this.emailFlag = (in.readInt() == 1) ? true : false;
		this.phoneFlag = (in.readInt() == 1) ? true : false;
		this.userID = in.readString();
		this.userPW = in.readString();
		this.confirmPw = in.readString();
		this.farm = in.readString();
		this.address = in.readString();
		in.readStringArray(this.imagePath);
		this.category = in.readString();
		this.introduction = in.readString();
		this.duplicateFlag = (in.readInt() == 1) ? true : false;
		this.agreeFlag = (in.readInt() == 1) ? true : false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<JoinFarmerData> CREATOR = new Parcelable.Creator<JoinFarmerData>() {

		@Override
		public JoinFarmerData createFromParcel(Parcel source) {

			return new JoinFarmerData(source);

		}

		@Override
		public JoinFarmerData[] newArray(int size) {

			return new JoinFarmerData[size];

		}

	};
}
