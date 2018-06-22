package com.ledgerleopard.sorvin.model;

public class QRPayload {
	public String did;
	public String verkey;

	public String token;

	private String baseUrl;
	private String path;

	public String sendbackUrl(){
		return baseUrl + path;
	}
}
