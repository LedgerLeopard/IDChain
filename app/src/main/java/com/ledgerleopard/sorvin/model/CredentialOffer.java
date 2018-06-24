package com.ledgerleopard.sorvin.model;

public class CredentialOffer {
    public String schema_id;
    public String cred_def_id;
    public String nonce;

    public CredentialOffer(String schema_id, String cred_def_id, String nonce) {
        this.schema_id = schema_id;
        this.cred_def_id = cred_def_id;
        this.nonce = nonce;
    }
}