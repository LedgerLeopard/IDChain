package com.ledgerleopard.sorvin.api.request;

public class OnboadringRequest {

	public String did;
	public String verkey;
	public String token;

	public OnboadringRequest(String did, String verkey, String token) {
		this.token = token;
		this.did = did;
		this.verkey = verkey;
	}
}
