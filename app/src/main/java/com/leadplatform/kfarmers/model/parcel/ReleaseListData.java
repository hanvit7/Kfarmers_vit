package com.leadplatform.kfarmers.model.parcel;

import android.os.Parcel;
import android.os.Parcelable;

public class ReleaseListData implements Parcelable {
	public static final String KEY = "ReleaseListData";

	private String PrimaryIndex;
	private String PrimaryName;
	private String SubIndex;
	private String SubName;

	public ReleaseListData() {

	}

	public String getPrimaryIndex() {
		return PrimaryIndex;
	}

	public void setPrimaryIndex(String primaryIndex) {
		PrimaryIndex = primaryIndex;
	}

	public String getPrimaryName() {
		return PrimaryName;
	}

	public void setPrimaryName(String primaryName) {
		PrimaryName = primaryName;
	}

	public String getSubIndex() {
		return SubIndex;
	}

	public void setSubIndex(String subIndex) {
		SubIndex = subIndex;
	}

	public String getSubName() {
		return SubName;
	}

	public void setSubName(String subName) {
		SubName = subName;
	}

	/***************************************************************/
	// Parcelable
	/***************************************************************/
	public ReleaseListData(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(PrimaryIndex);
		dest.writeString(PrimaryName);
		dest.writeString(SubIndex);
		dest.writeString(SubName);
	}

	private void readFromParcel(Parcel in) {
		this.PrimaryIndex = in.readString();
		this.PrimaryName = in.readString();
		this.SubIndex = in.readString();
		this.SubName = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ReleaseListData> CREATOR = new Parcelable.Creator<ReleaseListData>() {

		@Override
		public ReleaseListData createFromParcel(Parcel source) {

			return new ReleaseListData(source);

		}

		@Override
		public ReleaseListData[] newArray(int size) {

			return new ReleaseListData[size];

		}

	};
}
