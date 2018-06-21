package com.ledgerleopard.sorvin.model;

import com.google.gson.annotations.SerializedName;

public class QRPayload {
	public String did;

	public String nonce;

	@SerializedName("url")
	public String sendBackUrl;
}
