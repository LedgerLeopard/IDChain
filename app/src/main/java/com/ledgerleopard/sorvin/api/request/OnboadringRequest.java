package com.ledgerleopard.sorvin.api.request;

public class OnboadringRequest {

	public String did;
	public String verkey;

	public OnboadringRequest(String did, String verkey) {
		this.did = did;
		this.verkey = verkey;
	}
}
