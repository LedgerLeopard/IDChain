package com.ledgerleopard.sorvin.model;

import com.google.gson.annotations.SerializedName;

public class QRPayload {
	public String did;

	@SerializedName("url")
	public String sendBackUrl;
}
