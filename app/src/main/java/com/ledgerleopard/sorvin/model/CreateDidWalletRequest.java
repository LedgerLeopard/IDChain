package com.ledgerleopard.sorvin.model;

public class CreateDidWalletRequest {
	public String did;
	public String seed;
	public String crypto_type;
	public Boolean cid;

	public CreateDidWalletRequest(String did, String seed, String crypto_type, Boolean cid) {
		this.did = did;
		this.seed = seed;
		this.crypto_type = crypto_type;
		this.cid = cid;
	}
}
