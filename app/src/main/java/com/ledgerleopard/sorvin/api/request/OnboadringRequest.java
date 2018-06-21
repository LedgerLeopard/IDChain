package com.ledgerleopard.sorvin.api.request;

public class OnboadringRequest {

	public OnboadringPayload responsePayload;

	class OnboadringPayload {
		public String did;
		public String verkey;
		public String encryptedNonce;


		public OnboadringPayload(String did, String verkey, String encryptedNonce) {
			this.did = did;
			this.verkey = verkey;
			this.encryptedNonce = encryptedNonce;
		}
	}

	public OnboadringRequest(String did, String verkey, String encryptedNonce) {
		this.responsePayload = new OnboadringPayload(did, verkey, encryptedNonce);
	}
}
