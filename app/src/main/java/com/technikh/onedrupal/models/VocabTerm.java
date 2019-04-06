package com.technikh.onedrupal.models;

/*
 * Copyright (c) 2019. Nikhil Dubbaka from TechNikh.com under GNU AFFERO GENERAL PUBLIC LICENSE
 * Copyright and license notices must be preserved.
 * When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
 */

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class VocabTerm {
    @SerializedName("name")
    public ArrayList<fieldStringValue> name;
    @SerializedName("parent")
    public ArrayList<fieldIntTargetId> parentTid;
    @SerializedName("vid")
    public ArrayList<fieldTargetIdValue> vocabularyId;
    @SerializedName("tid")
    public ArrayList<fieldIntValue> tid;
    @SerializedName("uuid")
    public ArrayList<fieldStringValue> uuid;
}