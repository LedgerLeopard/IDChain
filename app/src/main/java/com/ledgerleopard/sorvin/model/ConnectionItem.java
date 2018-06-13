package com.ledgerleopard.sorvin.model;


import com.google.gson.annotations.SerializedName;

public class ConnectionItem {

	@SerializedName("my_did")
	public String myId;

	@SerializedName("their_did")
	public String theirId;

	@SerializedName("metadata")
	public String connectionName;

}
