package com.ledgerleopard.sorvin.api.response;

public class PostCredentialOfferResponse extends BaseResponse {

	public String credRevocId;
	public String revocRegDelta;
	public Credentials credentials;

	public class Credentials {
		public String schemaId;
		public String credDefId;
		public String revRegDefId;
	}
}

