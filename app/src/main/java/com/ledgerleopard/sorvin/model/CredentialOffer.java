package com.ledgerleopard.sorvin.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CredentialOffer {
    public String schema_id;
    public String cred_def_id;
    public String nonce;
	@SerializedName("key_correctness_proof")
    public KeyCorrectnessProof keyCorrectnessProof;

	public class KeyCorrectnessProof {
		public String c;
		@SerializedName("xz_cap")
		public String xzCap;
		@SerializedName("xr_cap")
		public List<List<String>> xrCap = new ArrayList<>();
	}
}