package com.ledgerleopard.sorvin.api.request;

public class OnboadringRequest {

	public OnboadringPayload responsePayload;

	class OnboadringPayload {
		public String did;
		public String verkey;

		public OnboadringPayload(String did, String verkey) {
			this.did = did;
			this.verkey = verkey;
		}
	}

	public OnboadringRequest(String did, String verkey) {
		this.responsePayload = new OnboadringPayload(did, verkey);
	}
}
