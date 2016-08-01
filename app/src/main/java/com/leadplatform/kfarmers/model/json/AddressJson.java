package com.leadplatform.kfarmers.model.json;

import java.io.Serializable;

public class AddressJson implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final String JIBUN = "1";
	public final String ROAD = "2";

	public String Idx;
	public String ShippingName;
	public String PhoneNo;
	public String ZipCode;
	public String Address;
	public String Address2;
	public String ZipCodeCategory;

	public String getIdx() {
		return Idx;
	}

	public void setIdx(String idx) {
		Idx = idx;
	}

	public String getShippingName() {
		return ShippingName;
	}

	public void setShippingName(String shippingName) {
		ShippingName = shippingName;
	}

	public String getPhoneNo() {
		return PhoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		PhoneNo = phoneNo;
	}

	public String getZipCode() {
		return ZipCode;
	}

	public void setZipCode(String zipCode) {
		ZipCode = zipCode;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public String getAddress2() {
		return Address2;
	}

	public void setAddress2(String address2) {
		Address2 = address2;
	}

	public String getZipCodeCategory() {
		return ZipCodeCategory;
	}

	public void setZipCodeCategory(String zipCodeCategory) {
		ZipCodeCategory = zipCodeCategory;
	}
}
