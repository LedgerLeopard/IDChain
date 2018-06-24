package com.ledgerleopard.sorvin.model;

public class QRPayload {
	public String did;
	public String verkey;

	public String token;

	private String baseUrl;
	private String path;


	public String getBaseUrl() {
		return baseUrl;
	}

	public String sendbackUrl(){
		return baseUrl + path;
	}
}
