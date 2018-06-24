package com.ledgerleopard.sorvin.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SchemaDefinition {

    @SerializedName("ver")
    public String ver;

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("version")
    public String version;

    @SerializedName("attrNames")
    public List<String> attrNames = null;

    @SerializedName("seqNo")
    public Integer seqNo;
}
